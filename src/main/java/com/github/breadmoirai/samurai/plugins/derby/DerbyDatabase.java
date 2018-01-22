/*    Copyright 2017 Ton Ly
 
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
package com.github.breadmoirai.samurai.plugins.derby;

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.samurai.command.CommandContext;
import com.github.breadmoirai.samurai.database.SQLUtil;
import com.github.breadmoirai.samurai.database.dao.PointDao;
import com.github.breadmoirai.samurai.points.PointSession;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DerbyDatabase implements CommandPlugin {

    private final String protocol = "jdbc:derby:";
    private final String dbName;

    private final Jdbi jdbi;

    public DerbyDatabase(String dbName) throws SQLException {
        this.dbName = dbName;
        boolean databaseExists = connectElseCreate();
        jdbi = Jdbi.create(protocol + dbName + ";");
        jdbi.installPlugin(new SqlObjectPlugin());
        if (!databaseExists) loadItems(false);
    }

    public <T, R> R openDao(Class<T> tClass, Function<T, R> function) {
        return jdbi.withExtension(tClass, function::apply);
    }

    public <T> void openDao(Class<T> tClass, Consumer<T> consumer) {
        jdbi.useExtension(tClass, consumer::accept);
    }

    public <E extends JdbiExtension> E getExtension(Function<Jdbi, E> function) {
        return function.apply(jdbi);
    }

    public PointSession getPointSession(long guildId, long memberId) {
        final PointSession pointSession = jdbi.withExtension(PointDao.class, extension -> extension.getSession(memberId, guildId));
        if (pointSession != null) {
            return pointSession;
        } else {
            jdbi.useExtension(PointDao.class, extension -> extension.insertUser(memberId, guildId));
        }
        return jdbi.withExtension(PointDao.class, extension -> extension.getSession(memberId, guildId));
    }

    /**
     * @return true if database exists, false if database was created
     */
    private boolean connectElseCreate() throws SQLException {
        boolean created = false;
        Connection initialConnection = null;
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            final String url = protocol + dbName + ";";
            initialConnection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            if (e.getErrorCode() == 40000
                    && e.getSQLState().equalsIgnoreCase("XJ004")) {
                initialConnection = DriverManager.getConnection(protocol + dbName + ";create=true");
                initializeTables(initialConnection);
                created = true;
            } else {
                SQLUtil.printSQLException(e);
            }
        } finally {
            if (initialConnection == null) {
                throw new SQLException("Could not connect nor create SamuraiDerbyDatabase");
            } else {
                initialConnection.close();
            }
        }
        return !created;
    }

    private void initializeTables(Connection connection) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(DerbyDatabase.class.getResourceAsStream("databaseInitializer.sql")))) {

            final Statement statement = connection.createStatement();
            for (String s : br.lines().collect(Collectors.joining()).split(";")) {
                statement.addBatch(s);
            }
            statement.executeBatch();

            connection.commit();
            statement.close();
        } catch (SQLException e) {
            SQLUtil.printSQLException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadItems(boolean deleteTables) {
        jdbi.useHandle(handle -> {
            try (BufferedReader br = deleteTables ? new BufferedReader(new InputStreamReader(DerbyDatabase.class.getResourceAsStream("ItemReset.sql"))) : null;
                 BufferedReader brItems = new BufferedReader(new InputStreamReader(DerbyDatabase.class.getResourceAsStream("Items.csv")));
                 BufferedReader brDrops = new BufferedReader(new InputStreamReader(DerbyDatabase.class.getResourceAsStream("Drops.csv")))) {

                if (deleteTables)
                    handle.createScript(br.lines().collect(Collectors.joining("\n"))).executeAsSeparateStatements();

                final PreparedBatch itemBatch = handle.prepareBatch("INSERT INTO ItemCatalog VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                brItems.readLine();
                String line;
                while ((line = brItems.readLine()) != null) {
                    final String[] values = line.split(",", 0);
                    System.out.println("item = " + Arrays.toString(values));
                    if (values.length != 15) continue;
                    for (int i = 0; i < 15; i++) {
                        final String value = values[i];
                        switch (i) {
                            case 0:
                                if (CommandContext.isNumber(value)) itemBatch.bind(i, Integer.parseInt(value));
                                break;
                            case 1:
                            case 2:
                            case 14:
                                if (value == null || value.isEmpty()) itemBatch.bindNull(i, Types.VARCHAR);
                                else itemBatch.bind(i, value);
                                break;
                            case 3:
                            case 5:
                                System.out.println("value = " + value);
                                if (value == null || value.isEmpty() || !CommandContext.isNumber(value)) {
                                    itemBatch.bindNull(i, Types.SMALLINT);
                                } else itemBatch.bind(i, Short.parseShort(value));
                                break;
                            case 12:
                            case 13:
                                if (value == null || value.isEmpty() || !CommandContext.isNumber(value))
                                    itemBatch.bindNull(i, Types.BIGINT);
                                else itemBatch.bind(i, Long.parseLong(value));
                                break;
                            default:
                                if (value == null || value.isEmpty() || !CommandContext.isFloat(value))
                                    itemBatch.bindNull(i, Types.DOUBLE);
                                else itemBatch.bind(i, Double.parseDouble(value));
                                break;
                        }
                    }
                    itemBatch.add();
                }
                System.out.println("ItemsInserted: " + Arrays.stream(itemBatch.execute()).sum());

                final PreparedBatch dropBatch = handle.prepareBatch("INSERT INTO DropRate VALUES (?, ?, ?)");
                brDrops.readLine();
                while ((line = brDrops.readLine()) != null) {
                    final String[] values = line.split(",", 0);
                    System.out.println("drops = " + Arrays.toString(values));
                    if (values.length != 3) continue;
                    for (int i = 0; i < 3; i++) {
                        final String value = values[i];
                        dropBatch.bind(i, Integer.parseInt(value));
                    }
                    dropBatch.add();
                }
                System.out.println("ItemDropsInserted: " + Arrays.stream(dropBatch.execute()).sum());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(BreadBotBuilder builder) {

    }
}