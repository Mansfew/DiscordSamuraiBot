/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package samurai.database;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author TonTL
 * @version 3/23/2017
 */
public class SQLUtil {

    public static void printSQLException(SQLException e) {

        while (e != null) {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).filter(s -> s.contains("samurai")).forEach(System.err::println);
            e = e.getNextException();
        }
    }
}
