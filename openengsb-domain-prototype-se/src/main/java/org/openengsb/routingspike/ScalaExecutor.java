/**

 Copyright 2009 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

package org.openengsb.routingspike;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import org.openengsb.util.IO;

import scala.tools.nsc.Interpreter;
import scala.tools.nsc.Settings;
import scala.tools.nsc.InterpreterResults.Result;

public class ScalaExecutor {

    private final Interpreter interpreter;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public enum ScalaResult {
        SUCCESS, INCOMPLETE, ERROR;

        public static ScalaResult valueOf(int code) {
            switch (code) {
            case 496257788:
                return SUCCESS;
            case 783042369:
                return ERROR;
            case 462041029:
                return INCOMPLETE;
            default:
                throw new IllegalArgumentException();
            }
        }
    }

    public ScalaExecutor() {
        interpreter = new Interpreter(new Settings(), new PrintWriter(baos, true));
    }

    public void execute(String code) {
        baos.reset();
        Result result = interpreter.interpret(code);

        ScalaResult r;

        try {
            r = ScalaResult.valueOf(result.$tag());
        } catch (RemoteException e) {
            throw new AssertionError(e);
        }

        if (r == ScalaResult.ERROR) {
            throw new ScalaExecutionException(baos.toString());
        }
    }

    public void execute(File file) {
        execute(readFile(file));
    }

    public void bind(String name, Class<?> clazz, Object obj) {
        interpreter.bind(name, clazz.getName(), obj);
    }

    private String readFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            return builder.toString();
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find script");
        } catch (IOException e) {
            System.out.println("I/O Error");
        } finally {
            IO.closeQuietly(reader);
        }

        return null;
    }
}
