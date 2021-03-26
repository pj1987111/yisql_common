package com.zhy.yisql.common.utils.shell

import java.io.{File, FileWriter, RandomAccessFile}
import java.util.UUID

import org.apache.log4j.LogManager

import scala.sys.process.{ProcessLogger, _}

object ShellCommand extends TFileWriter {
    @transient private lazy val logger = LogManager.getLogger(classOf[ShellCommand])

    def exec(shellStr: String): String = {
        try {
            exec("", shellStr)
        } catch {
            case e: Exception =>
                logger.error("exec fail", e)
                ""
        }
    }

    def sshExec(keypath: String, hostname: String, username: String, command: String, execute_user: String, dryRun: Boolean = false): String = {
        val localPath = s"/tmp/${UUID.randomUUID().toString}"
        writeToFile(localPath, "#!/bin/bash\nsource /etc/profile\n" + command)
        val copyScriptCommand = List("scp", "-oStrictHostKeyChecking=no", "-oUserKnownHostsFile=/dev/null", "-i", keypath, localPath,
            username + "@" + hostname + ":" + localPath)
        logger.debug("copy shell scirpt: " + copyScriptCommand.mkString(" "))
        if (!dryRun) {
            execCmd(copyScriptCommand.mkString(" "))
        }
        val execute_command = if (execute_user == username) {
            "chmod u+x " + localPath + ";/bin/bash " + localPath
        } else {
            val tmp = "chmod u+x " + localPath + ";/bin/bash " + localPath
            if (!dryRun) {
                execCmd(
                    List("ssh", "-oStrictHostKeyChecking=no", "-oUserKnownHostsFile=/dev/null", "-i", keypath,
                        username + "@" + hostname, "chown -R  " + execute_user + " " + localPath).mkString(" "))
            }
            "'su - " + execute_user + " -c \"" + tmp + "\"'"
        }

        val sshExecuteCommand = List("ssh", "-oStrictHostKeyChecking=no", "-oUserKnownHostsFile=/dev/null", "-i", keypath,
            username + "@" + hostname, execute_command)

        logger.debug("execute script: " + sshExecuteCommand.mkString(" "))

        if (!dryRun) {
            val res = execWithUserAndExitValue(null, sshExecuteCommand.mkString(" "), -1)
            logger.info(s"execute: \n[code: ${res._1}] \n[out： ${res._2}] \n[error: ${res._3}]")
            if (res._1 != 0) {
                throw new RuntimeException(s"fail to start mlsql instance. execute: \n[code: ${res._1}] \n[out： ${res._2}] \n[error: ${res._3}]")
            }
            res._2
        } else {
            sshExecuteCommand.mkString(" ")
        }

    }

    def wrapCommand(user: String, fileName: String) = {
        if (user != null && !user.isEmpty) {
            s"su - $user /bin/bash -c '/bin/bash /tmp/$fileName'"
        } else s"/bin/bash /tmp/$fileName"
    }

    def readFile(dir: String, offset: Long, readSize: Long): (Long, String) = {
        var fr = new RandomAccessFile(s"$dir/stderr", "r")
        fr = if (fr.length() > 0) fr
        else {
            fr.close()
            new RandomAccessFile(s"$dir/stdout", "r")
        }
        try {
            val localOffset = offset match {
                case x if x < 0 && fr.length() >= 10000 => 10000
                case x if x < 0 && fr.length() < 10000 => 0
                case x if x >= 0 => offset
            }
            fr.seek(localOffset)

            val bytes = new Array[Byte](readSize.toInt)
            val len = fr.read(bytes)
            if (len > 0)
                (fr.length(), new String(bytes, 0, len, "UTF-8"))
            else
                (0, "")
        } finally fr.close()

    }


    def exec(user: String, shellStr: String) = {
        logger.debug("shell " + shellStr)
        val fileName = System.currentTimeMillis() + "_" + Math.random() + ".sh"
        writeToFile("/tmp/" + fileName, "#!/bin/bash\nsource /etc/profile\n" + shellStr)
        s"chmod u+x /tmp/$fileName".!
        val result = wrapCommand(user, fileName).!!.trim
        s"rm /tmp/$fileName".!
        result
    }

    def execCmd(shellStr: String) = {
        shellStr.!!
    }

    def execCmdV2(command: os.Shellable*) = {
        val cr = os.proc(command).call(check = false)
        cr
    }

    def execCmdV2WithProcessing(f: (String) => Unit, command: os.Shellable*) = {
        val cr = os.proc(command).spawn()
        while (cr.stdout.available() > 0 || cr.isAlive()) {
            f(cr.stdout.readLine())
        }
        cr
    }

    def execFile(fileName: String) = {
        logger.debug("exec file " + fileName)

        s"chmod u+x ${fileName}".!
        val result = s"/bin/bash $fileName".!!.trim
        result
    }

    /*
      todo: timeout supported
     */
    def execWithExitValue(shellStr: String, timeout: Long = AsyncShellCommand.defaultTimeOut): (Int, String, String) = {
        execWithUserAndExitValue("", shellStr, timeout)
    }

    def execWithUserAndExitValue(user: String, shellStr: String, timeout: Long = AsyncShellCommand.defaultTimeOut) = {
        val out = new StringBuilder
        val err = new StringBuilder
        val et = ProcessLogger(
            line => out.append(line + "\n"),
            line => err.append(line + "\n"))
        logger.info("shell " + shellStr)
        val fileName = System.currentTimeMillis() + "_" + Math.random() + ".sh"
        writeToFile("/tmp/" + fileName, "#!/bin/bash\nsource /etc/profile\n" + shellStr)
        s"chmod u+x /tmp/$fileName".!
        val pb = Process(wrapCommand(user, fileName))
        val exitValue = pb ! et
        s"rm /tmp/$fileName".!
        logger.debug(s"rm /tmp/$fileName")
        (exitValue, err.toString().trim, out.toString().trim)
    }


    def progress(filePath: String, offset: Long, size: Int): (Long, String, Long) = {
        if (!new File(filePath).exists()) return (-1, "", 0)

        val fr = new RandomAccessFile(filePath, "r")
        if (fr.length() <= offset) {
            fr.close()
            return (-1, "", 0)
        }
        try {
            val temp = 4 * 1024 //if (fr.length() > 4 * 1024) 4 * 1024 else 0
            var newSize: Long = if (size <= 0 || size > 1024 * 1024) temp else size
            val newOffset = if (offset == -1 && fr.length() > temp) fr.length() - temp else if (offset == -1) 0 else offset
            fr.seek(newOffset)
            if (newOffset + newSize >= fr.length()) newSize = fr.length() - newOffset
            val bytes = new Array[Byte](newSize.toInt)
            fr.readFully(bytes)
            (fr.getFilePointer, new String(bytes, "UTF-8"), fr.length())
        } finally fr.close()

    }


}

class ShellCommand

trait TFileWriter {
    def using[A <: {def close() : Unit}, B](param: A)(f: A => B): B =
        try {
            f(param)
        } finally {
            param.close()
        }

    def writeToFile(fileName: String, data: String) =
        using(new FileWriter(fileName)) {
            fileWriter => fileWriter.append(data)
        }
}

