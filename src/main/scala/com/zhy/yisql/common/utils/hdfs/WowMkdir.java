/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhy.yisql.common.utils.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.shell.CommandFormat;
import org.apache.hadoop.fs.shell.PathData;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

/**
 * Create the given dir
 */

public class WowMkdir extends WowFsCommand {


    public static final String NAME = "mkdir";
    public static final String USAGE = "[-p] <path> ...";
    public static final String DESCRIPTION =
            "Create a directory in specified location.\n" +
                    "-p: Do not fail if the directory already exists";

    private boolean createParents;

    public WowMkdir(Configuration conf, String basePath, PrintStream out, PrintStream error) {
        super(conf, basePath, out, error);
    }

    @Override
    protected void processOptions(LinkedList<String> args) {
        CommandFormat cf = new CommandFormat(1, Integer.MAX_VALUE, "p");
        cf.parse(args);
        createParents = cf.getOpt("p");
        redefineBaseDir(args);
    }

    @Override
    protected void processPath(PathData item) throws IOException {
        if (item.stat.isDirectory()) {
            if (!createParents) {
                throw new PathExistsException(cleanPath(item.toString()));
            }
        } else {
            throw new PathIsNotDirectoryException(cleanPath(item.toString()));
        }
    }

    @Override
    protected void processNonexistentPath(PathData item) throws IOException {
        // check if parent exists. this is complicated because getParent(a/b/c/) returns a/b/c, but
        // we want a/b
        if (!item.fs.exists(new Path(item.path.toString()).getParent()) && !createParents) {
            throw new PathNotFoundException(item.toString());
        }
        if (!item.fs.mkdirs(item.path)) {
            throw new PathIOException(item.toString());
        }
    }
}
