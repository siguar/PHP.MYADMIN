package com.company;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {
        Scanner in = new Scanner(System.in);
        for (; ; ) {
            String action = "";
            do {
                System.out.print("Wybierz akcje (dodaj/sprzedaz/wyswietl): ");
                action = in.nextLine();
            } while (!action.equalsIgnoreCase("dodaj") && !action.equalsIgnoreCase("wyswietl") && !action.equalsIgnoreCase("sprzedaz"));
            if (action.equalsIgnoreCase("wyswietl")) {
                System.out.print("Podaj nazwe tabeli: ");
                String table = in.nextLine();
                showTable(table);
            } else if (action.equalsIgnoreCase("dodaj")) {
                System.out.print("Podaj nazwe tabeli: ");
                String table = in.nextLine();
                SQLConnection connection = connectDB();
                ResultSet resultSet = connection.getResults("select * from " + table);
                ResultSetMetaData rsMetaData = resultSet.getMetaData();
                ArrayList<Integer> columnTypes = new ArrayList<>();
                int count = rsMetaData.getColumnCount();
                ArrayList<String> columns = new ArrayList<String>();
                for (int i = 1; i <= count; i++) {
                    if (!rsMetaData.getColumnName(i).equalsIgnoreCase("id")) {
                        columns.add(rsMetaData.getColumnName(i));
                        columnTypes.add(rsMetaData.getColumnType(i));
                    }
                }
                String query = "insert into " + table + " (";
                String columnsWithCommas = String.join(", ", columns);
                query += columnsWithCommas + ") values (";
                ArrayList<String> values = new ArrayList<>();
                int index = 0;
                for (String column : columns) {
                    boolean isInt = false;
                    if (columnTypes.get(index) == 4) {
                        isInt = true;
                    }
                    String data = "";
                    boolean correct = false;
                    do {
                        System.out.print("Podaj \"" + column + "\": ");
                        data = in.nextLine();
                        if (data.matches("\\d+")) {
                            //integer
                            if (isInt) {
                                correct = true;
                            }
                        } else {
                            //string
                            if (!isInt) {
                                correct = true;
                            }
                        }
                    } while (!correct);
                    values.add("\"" + data + "\"");
                    index++;
                }

                query += String.join(", ", values) + ");";


                System.out.println("Wykonano polecenie:" + query);
                connection.execute(query);
            } else {
                SQLConnection connection = connectDB();
                ResultSet resultSet = null;
                int pozycja = 0;
                do {
                    showTable("bron");
                    System.out.print("Wybierz pozycje: ");
                    pozycja = in.nextInt();
                    resultSet = connection.getResults("select * from bron inner join id_typow_broni using(id_typu_broni) where id=" + pozycja + ";");
                } while (!resultSet.first());
                int wymaganePozwolenie = resultSet.getInt("pozwolenie");

                resultSet = null;
                do {
                    showTable("tablica_klientow");
                    System.out.println("Wybierz klienta: ");
                    int klient = in.nextInt();
                    resultSet = connection.getResults("select * from tablica_klientow inner join typy_pozwolenia using(pozwolenie) where id=" + klient + ";");
                } while (!resultSet.first());
                int pozwolenieKlienta = resultSet.getInt("pozwolenie");

                if(wymaganePozwolenie == pozwolenieKlienta) {
                    connection.execute("DELETE FROM bron WHERE id=" + pozycja + ";");
                    System.out.println("Bron zostala sprzedana pomyslnie!");
                } else {
                    System.out.println("Klient nie posiada uprawnien do kupienia tej broni.");
                }

            }

        }

    }

    public static void showTable(String name) {
        int columnSize = 30;
        try {
            SQLConnection connection = connectDB();
            String query = "";
            if(name.equalsIgnoreCase("bron")) {
                query = "with test as (select id_typu_broni, kolor, rodzaj_amunicji, magazynek_amunicji, typ_broni, rodzaj, ilosc_amunicji_w_magazynku, rodzaj_czesci, cena_czesci, ROW_NUMBER() OVER( PARTITION BY id_typu_broni ORDER BY id_typu_broni ) as count from bron inner join id_typow_broni using(id_typu_broni) inner join rodzaje_amunicji on bron.rodzaj_amunicji = rodzaje_amunicji.id inner join magazynki_amunicji on bron.magazynek_amunicji = magazynki_amunicji.id inner join czesci_tuningowe using(id_typu_broni) ) select * from test";
            } else if(name.equalsIgnoreCase("tablica_klientow")) {
                query = "select * from tablica_klientow generate_series inner join typy_pozwolenia using(pozwolenie);";
            } else {
                query = "select * from " + name;
            }
            ResultSet resultSet = connection.getResults(query);
            ArrayList<String> columns = getColumnsNames(resultSet);
            String data = "";
            for(String columnName : columns) {
                int ilosc_znakow = columnSize - columnName.length();
                data += columnName;
                for(int i=0;i<ilosc_znakow;i++) {
                    data += " ";
                }
            }
            System.out.println(data);


            data = "";
            for(String columnName : columns) {
                int ilosc = columnSize;
                if(columnName.length() > columnSize) {
                    ilosc = columnName.length();
                }
                for(int i=0;i<ilosc;i++){
                    data += "-";
                }
            }
            System.out.println(data);


            while(resultSet.next()) {
                data = "";
                for(String column : columns) {
                    String string = resultSet.getString(column);
                    int ilosc_znakow = columnSize - string.length();
                    data += string;
                    for(int i=0;i<ilosc_znakow;i++) {
                        data += " ";
                    }
                }
                System.out.println(data);
            }

            System.out.println();
            connection.close();
        } catch(SQLException e) {
            System.out.println("Wystapil blad:");
            e.printStackTrace();
        }
    }

    public static SQLConnection connectDB() throws SQLException {
        return new SQLConnection("jdbc:mysql://193.70.94.34:3306/db_74676?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "db_74676", "NMSHOY1GgYvj");
    }

    public static ArrayList<String> getColumnsNames(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int count = rsMetaData.getColumnCount();
        ArrayList<String> columns = new ArrayList<String>();
        for(int i = 1; i<=count; i++) {
            columns.add(rsMetaData.getColumnName(i));
        }

        return columns;
    }
}
