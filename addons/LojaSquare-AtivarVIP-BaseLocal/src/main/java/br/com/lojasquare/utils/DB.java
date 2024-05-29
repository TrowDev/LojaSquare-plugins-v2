package br.com.lojasquare.utils;

import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import br.com.lojasquare.LojaSquare;
import org.bukkit.Bukkit;

public class DB {

    private Connection conn;
    private File file;
    private Statement stmt;

    private DB(File f) {
        file = f;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + file);
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DB(String urlconn) {
        urlconn = replacer(urlconn);
        try {
            conn = DriverManager.getConnection(urlconn);
            stmt = conn.createStatement();
        } catch (Exception e) {
            throwException(urlconn, e);
        }
    }

    public static DB load(File f) {
        return new DB(f);
    }

    public static DB load(String f) {
        return new DB(new File(f));
    }

    public static DB load(String host, String database, String user, String pass) {
        try {
            return new DB("jdbc:mysql://" + host + "/" + database + "?" + "user="
                    + user + "&password=" + URLEncoder.encode(pass, "UTF-8") + "&autoReconnect=true");
        } catch (Exception e) {
            throwException("On load method...", e);
        }
        return null;
    }

    public static String replacer(String outBuffer) {
        String data = outBuffer;
        try {
            data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B");
//			data = URLEncoder.encode(data, "UTF-8");
//			.replaceAll("&", "");
//			data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("§4["+LojaSquare.getInstance().getDescription().getName()+"] §cErro ao adaptar caracteres especiais da senha.");
        }
        return data;
    }

    public void update(String q) {
        try {
            stmt.executeUpdate(q);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet query(String q) {
        try {
            return stmt.executeQuery(q);
        } catch (Exception e) {
        }
        return null;
    }

    public void close() {
        try {
            stmt.close();
            conn.close();
        } catch (Exception e) {
        }
    }

    public boolean isConnected() {
        try {
            return stmt != null && conn != null && !stmt.isClosed()
                    && !conn.isClosed();
        } catch (Exception e) {
        }
        return false;
    }

    public Connection getConnection() {
        return conn;
    }

    private static void throwException(String urlconn, Exception e) {
        if (LojaSquare.getInstance().canDebug()) {
            e.printStackTrace();
            Bukkit.getConsoleSender()
                    .sendMessage(
                            "§4["
                                    + LojaSquare.getInstance().getDescription().getName()
                                    + "] §cErro ao realizar conexao com o Banco de Dados. URL Connect: §a"
                                    + urlconn + "§c. Erro: §a"
                                    + e.getMessage());
        } else {
            Bukkit.getConsoleSender()
                    .sendMessage(
                            "§4["
                                    + LojaSquare.getInstance().getDescription().getName()
                                    + "] §cErro ao realizar conexao com o Banco de Dados. Erro: §a"
                                    + e.getMessage());
        }
    }

}