import org.json.JSONObject;

import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Search of a film in a database from a language of defined keywords
 * @author Dray Raphael
 * @version 1.0
 */
public class RechercheFilm {
    private String SQLiteFilename;
    private Connection dbConnection;

    private static final String DB_URL = "jdbc:sqlite:";

    /**
     * Constructor that connect to the SQLite Database given in parameter
     * @param SQLiteFilename The File *.sqlite which is the SQLite Database
     * @author Dray Raphael
     * @see Connection
     */
    public RechercheFilm(final String SQLiteFilename) {
        this.SQLiteFilename = SQLiteFilename;
        this.dbConnection = connect(this.SQLiteFilename);
        System.out.println("Connection to database " + this.SQLiteFilename + " has been established...");
    }

    /**
     * File stocked accessor
     * @return The name of the SQLite stocked file
     * @author Dray Raphael
     * @see String
     */
    public String getSQLiteFilename() {
        return this.SQLiteFilename;
    }

    /**
     * Connection to the Database accessor
     * @return The connection to the Database
     * @author Dray Raphael
     * @see Connection
     */
    public Connection getDbConnection() {
        return this.dbConnection;
    }

    /**
     * Static Method of connection to the Database
     * @param SQLiteFilename The name of the file of the Database which we want to connect to
     * @return The established connection to the Database
     * @throws RuntimeException If we didn't succeed to connect to the Database
     * @author Dray Raphael
     * @see Connection
     * @see DriverManager
     * @see SQLException
     */
    private static Connection connect(final String SQLiteFilename) {
        try {
            return DriverManager.getConnection(DB_URL + SQLiteFilename);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the connection to the Database if we are connected
     * @author Dray Raphael
     * @see Connection
     * @see SQLException
     */
    public void fermeBase() {
        if(this.dbConnection != null) {
            try {
                this.dbConnection.close();
                System.out.println("Database " + this.SQLiteFilename + " has been closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Principal function to search about the request
     * It returns the result of the SQL request in the SQLite Database
     * The result is returned as JSON Object.
     * @param str The Request
     * @return The result derived from the toString() method of StringWriter object
     * which we have written in it the JSON of the SQL request.
     * @author Dray Raphael
     * @see JSONObject
     * @see StringWriter
     * @see Analyzer
     * @see InfoFilm
     */
    public String retrouve(final String str) {
        StringWriter output = new StringWriter();
        JSONObject json = new JSONObject();

        String[] verification = str.split(" ");

        if(verification.length < 2) {
            json.put("erreur", "Vous devez entrer au moins deux arguments !");
            json.write(output);
            return output.toString();
        }

        else if(verification[0].equals("OU")) {
            json.put("erreur", "Vous ne pouvez pas commencer une requete par OU");
            json.write(output);
            return output.toString();
        }

        else if(isKeyword(verification[verification.length - 1])) {
            json.put("erreur", "La recherche ne peut pas se terminer par un mot-clef !");
            json.write(output);
            return output.toString();
        }

        else {
            for(int i = 1; i < verification.length; ++i)
                if(isKeyword(verification[i - 1]) && isKeyword(verification[i])) {
                    json.put("erreur", "Vous ne pouvez pas associer deux mot-clefs excepté OU");
                    json.write(output);
                    return output.toString();
                }
        }


        Analyzer an = new Analyzer(str);
        final LinkedList<LinkedList<String>> requete = an.getRequestedList();
        LinkedList<LinkedList<Integer>> results = new LinkedList<>();

        for (LinkedList<String> request : requete) {
            for(String s : request) {
                String[] splittedString = s.split(";");

                switch (splittedString[0].toUpperCase()) {
                    case "TITRE":
                        System.out.println("Retrieving for TITRE keyword..");
                        results.addLast(TitreRequest(splittedString[1]));
                        break;
                    case "DE":
                        System.out.println("Retrieving for DE keyword..");
                        String[] splitSplittedString = splittedString[1].split(" ");
                        String arg1 = splitSplittedString[0];
                        String arg2 = splitSplittedString.length > 1 ? splitSplittedString[1] : "%";
                        results.addLast(DeAvecRequest(arg1, arg2));
                        break;
                    case "AVEC":
                        System.out.println("Retrieving for AVEC keyword..");
                        results.addLast(DeAvecRequest(splittedString[1].split(" ")[0],
                                                      splittedString[1].split(" ")[1]));
                        break;
                    case "PAYS":
                        System.out.println("Retrieving for PAYS keyword..");
                        results.addLast(PaysRequest(splittedString[1].replace(' ', '-').toUpperCase()));
                        break;
                    case "EN":
                        System.out.println("Retrieving for EN keyword..");
                        results.addLast(EnRequest(Integer.parseInt(splittedString[1])));
                        break;
                    case "AVANT":
                        System.out.println("Retrieving for AVANT keyword..");
                        results.addLast(AvantRequest(Integer.parseInt(splittedString[1])));
                        break;
                    case "APRES":
                        System.out.println("Retrieving for APRES keyword..");
                        results.addLast(ApresRequest(Integer.parseInt(splittedString[1])));
                        break;
                }
            }

        }

        System.out.println("Comparing results..");
        LinkedList<Integer> result = getCommonId(results);

        System.out.println("Building InfoFilm for Films..");
        LinkedList<InfoFilm> films = new LinkedList<>();
        boolean isMaxed = false;
        int max;
        if(result.size() > 100) {
            max = 100;
            isMaxed = true;
        } else
            max = result.size();

        for (int i = 0; i < max; i++) {
            int id = result.get(i);
            films.addLast(getInfoFilm(id));
        }

        json.put("resultat", films.toString());
        if(isMaxed)
            json.put("info", "Résultat limité à 100 films");
        json.write(output);

        return output.toString();
    }

    /**
     * Return a SQL request for the keyword TITRE with the name given in parameter
     * @param name The title of the film that we are searching for
     * @return A LinkedList<Integer> that contains all the idx found about the request
     * @author Dray Raphael
     * @see ResultSet
     * @see PreparedStatement
     * @see Connection
     * @see SQLException
     */
    private LinkedList<Integer> TitreRequest(final String name) {
        ResultSet resultSet = null;
        PreparedStatement statement;
        LinkedList<Integer> result = new LinkedList<>();

        try {
            statement = this.dbConnection.prepareStatement("SELECT id_film " +
                                                                "FROM (SELECT id_film " +
                                                                "FROM films " +
                                                                "WHERE titre " +
                                                                "LIKE '%' || replace(?, ' ', '%') || '%' " +
                                                                "UNION " +
                                                                "SELECT id_film " +
                                                                "FROM autres_titres " +
                                                                "WHERE titre " +
                                                                "LIKE '%' || replace(?, ' ', '%') || '%') x");
            statement.setString(1, name);
            statement.setString(2, name);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            if(this.dbConnection != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    this.dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        while(true) {
            try {
                assert resultSet != null;
                if (!resultSet.next()) break;
                else result.addLast(resultSet.getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    /**
     * Return a SQL request for the keyword AVEC or DE with the name and first name given in parameter
     * @param firstName The name of the actor or director
     * @param lastName The first name of the actor or director
     * @return A LinkedList<Integer> that contains all the idx found about the request
     * @author Dray Raphael
     * @see ResultSet
     * @see PreparedStatement
     * @see Connection
     * @see SQLException
     * @see Statement
     */
    private LinkedList<Integer> DeAvecRequest(final String firstName, final String lastName) {
        ResultSet resultSet = null;
        PreparedStatement statement;
        LinkedList<Integer> result = new LinkedList<>();

        try {
            statement = this.dbConnection.prepareStatement("SELECT id_personne " +
                                                                "FROM personnes " +
                                                                "WHERE nom = ? AND prenom = ? " +
                                                                "OR nom = ? AND prenom = ? " +
                                                                "OR nom = ?");

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, lastName);
            statement.setString(4, firstName);
            statement.setString(5, firstName);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            if(this.dbConnection != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    this.dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        while(true) {
            try {
                assert resultSet != null;
                if(!resultSet.next()) break;
                else {
                    String sql = "SELECT id_film FROM generique WHERE id_personne = " + resultSet.getInt(1);
                    try {
                        Statement stmt = this.dbConnection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);

                        while (rs.next())
                            result.addLast(rs.getInt(1));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Return a SQL request for the keyword PAYS with the name or the country code given in parameter
     * @param name The name or country code
     * @return A LinkedList<Integer> that contains all the idx found about the request
     * @author Dray Raphael
     * @see ResultSet
     * @see PreparedStatement
     * @see Connection
     * @see SQLException
     * @see Statement
     */
    private LinkedList<Integer> PaysRequest(final String name) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        LinkedList<Integer> result = new LinkedList<>();

        try {
            if(name.length() > 2)
                statement = this.dbConnection.prepareStatement("SELECT code " +
                        "FROM pays " +
                        "WHERE nom " +
                        "LIKE '%' || replace(?, ' ', '%')");
            else
                statement = this.dbConnection.prepareStatement("SELECT id_film " +
                                                                   "FROM films " +
                                                                   "WHERE pays " +
                                                                   "LIKE '%' || replace(?, ' ', '%')");

            statement.setString(1, name);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            if(this.dbConnection != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    this.dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

        }

        while(true) {
            try {
                assert resultSet != null;
                if(!resultSet.next()) break;
                else {
                    if(name.length() > 2) {
                        String sql = "SELECT id_film FROM films WHERE pays = '" + resultSet.getString(1) + "'";
                        try {
                            Statement stmt = this.dbConnection.createStatement();
                            ResultSet rs = stmt.executeQuery(sql);

                            while (rs.next())
                                result.addLast(rs.getInt(1));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    else while (resultSet.next())
                        result.addLast(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Return a SQL request for the keyword EN with the release date given in parameter
     * @param year The release date
     * @return A LinkedList<Integer> that contains all the idx found about the request
     * @author Dray Raphael
     * @see ResultSet
     * @see PreparedStatement
     * @see Connection
     * @see SQLException
     */
    private LinkedList<Integer> EnRequest(final int year) {
        ResultSet resultSet = null;
        PreparedStatement statement;
        LinkedList<Integer> result = new LinkedList<>();

        try {
            statement = this.dbConnection.prepareStatement("SELECT id_film " +
                                                               "FROM films " +
                                                               "WHERE annee = ?");
            statement.setInt(1, year);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            if(this.dbConnection != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    this.dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        while(true) {
            try {
                assert resultSet != null;
                if(!resultSet.next()) break;
                else result.addLast(resultSet.getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Return a SQL request for the keyword AVANT with the year before the release of the film given in parameter
     * @param year The year before the release date of the film
     * @return A LinkedList<Integer> that contains all the idx found about the request
     * @author Dray Raphael
     * @see ResultSet
     * @see PreparedStatement
     * @see Connection
     * @see SQLException
     */
    private LinkedList<Integer> AvantRequest(final int year) {
        ResultSet resultSet = null;
        PreparedStatement statement;
        LinkedList<Integer> result = new LinkedList<>();

        try {
            statement = this.dbConnection.prepareStatement("SELECT id_film " +
                                                               "FROM films " +
                                                               "WHERE annee < ?");
            statement.setInt(1, year);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            if(this.dbConnection != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    this.dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        while(true) {
            try {
                assert resultSet != null;
                if(!resultSet.next()) break;
                else result.addLast(resultSet.getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Return a SQL request for the keyword APRES with the year after the release of the film given in parameter
     * @param year The year after the release date of the film
     * @return A LinkedList<Integer> that contains all the idx found about the request
     * @author Dray Raphael
     * @see ResultSet
     * @see PreparedStatement
     * @see Connection
     * @see SQLException
     */
    private LinkedList<Integer> ApresRequest(final int year) {
        ResultSet resultSet = null;
        PreparedStatement statement;
        LinkedList<Integer> result = new LinkedList<>();

        try {
            statement = this.dbConnection.prepareStatement("SELECT id_film " +
                                                               "FROM films " +
                                                               "WHERE annee > ?");
            statement.setInt(1, year);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            if(this.dbConnection != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    this.dbConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        while(true) {
            try {
                assert resultSet != null;
                if(!resultSet.next()) break;
                else result.addLast(resultSet.getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Static Method that determine the common elements between two linked lists
     * (Intersection of two linked list) of integers and
     * return those elements as a Linked List of Integer
     * @param results The result of all the SQL request done
     * @return A LinkedList<Integer> that contains all the idx that they need to be evaluate to get their InfoFilm
     * @author Dray Raphael
     * @see LinkedList
     */
    private static LinkedList<Integer> getCommonId(LinkedList<LinkedList<Integer>> results) {
        LinkedList<Integer> result = new LinkedList<>();
        LinkedList<Integer> tempResult = results.getFirst();

        if(results.size() > 1) {
            for (int i = 1; i < results.size(); ++i)
                for (int id : tempResult) {
                    for (int id2 : results.get(i))
                        if (id == id2 && !result.contains(id)) result.addLast(id);

                    tempResult = result;
                }

            return result;
        }

        else return tempResult;
    }

    /**
     * Return a SQL request to get the title of the film from its id
     * @param id The id_film
     * @return The title of the film
     * @author Dray Raphael
     * @see Statement
     * @see Connection
     * @see ResultSet
     * @see SQLException
     * @see RuntimeException
     */
    private String getTitre(int id) {
        String sql = "SELECT titre FROM films WHERE id_film = " + id;
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot Be Reached !");
    }

    /**
     * Return a SQL request to get the country of the film from its id
     * @param id The id_film
     * @return The country of the film
     * @author Dray Raphael
     * @see Statement
     * @see ResultSet
     * @see Connection
     * @see SQLException
     * @see RuntimeException
     */
    private String getPays(int id) {
        String sqlCodePays = "SELECT pays FROM films WHERE id_film = " + id;
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlCodePays);

            String sqlPays = "SELECT nom FROM pays WHERE code = '" + rs.getString(1) + "'";
            Statement stmt2 = this.dbConnection.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sqlPays);

            return rs2.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot Be Reached !");
    }

    /**
     * Return a SQL request to get the release date of the film from its id
     * @param id The id_film
     * @return The release date of the film
     * @author Dray Raphael
     * @see Statement
     * @see Connection
     * @see ResultSet
     * @see SQLException
     * @see RuntimeException
     */
    private int getAnnee(int id) {
        String sql = "SELECT annee FROM films WHERE id_film = " + id;
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot Be Reached !");
    }

    /**
     * Return a SQL request to get the time of the film from its id
     * @param id The id_film
     * @return The time of the film
     * @author Dray Raphael
     * @see Statement
     * @see Connection
     * @see ResultSet
     * @see SQLException
     * @see RuntimeException
     */
    private int getDuree(int id) {
        String sql = "SELECT duree FROM films WHERE id_film = " + id;
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot Be Reached !");
    }

    /**
     * Return a SQL request to get the other titles of the film from its id
     * @param id The id_film
     * @return The other titles of the film
     * @author Dray Raphael
     * @see ArrayList
     * @see Statement
     * @see ResultSet
     * @see Connection
     * @see SQLException
     */
    private ArrayList<String> getAutresTitres(int id) {
        ArrayList<String> autresTitres = new ArrayList<>();
        String sql = "SELECT titre FROM autres_titres WHERE id_film = " + id;
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next())
                autresTitres.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return autresTitres;
    }

    /**
     * Return a SQL request to get the NomPersonne object from its id
     * @param id The d_personne
     * @return An object {NomPersonne}
     * @author Dray Raphael
     * @see NomPersonne
     * @see Statement
     * @see Connection
     * @see ResultSet
     * @see SQLException
     * @see RuntimeException
     */
    private NomPersonne getNomPersonne(int id) {
        String sql = "SELECT nom, prenom FROM personnes WHERE id_personne = " + id;
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            return new NomPersonne(rs.getString(1), rs.getString(2));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cannot Be Reached !");
    }

    /**
     * Return a SQL request to get the directors of the film from its id
     * @param id The id_film
     * @return The directors of the film
     * @author Dray Raphael
     * @see ArrayList
     * @see NomPersonne
     * @see Statement
     * @see Connection
     * @see ResultSet
     * @see SQLException
     */
    private ArrayList<NomPersonne> getRealisateurs(int id) {
        ArrayList<NomPersonne> realisateurs = new ArrayList<>();
        String sql = "SELECT id_personne FROM generique WHERE id_film = " + id + " AND role = 'R'";
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next())
                realisateurs.add(getNomPersonne(rs.getInt(1)));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return realisateurs;
    }

    /**
     * Return a SQL request to get the actors of the film from its id
     * @param id The id_film
     * @return The actors of the film
     * @author Dray Raphael
     * @see ArrayList
     * @see NomPersonne
     * @see Statement
     * @see Connection
     * @see ResultSet
     * @see SQLException
     */
    private ArrayList<NomPersonne> getActeurs(int id) {
        ArrayList<NomPersonne> acteurs = new ArrayList<>();
        String sql = "SELECT id_personne FROM generique WHERE id_film = " + id + " AND role = 'A'";
        try {
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next())
                acteurs.add(getNomPersonne(rs.getInt(1)));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return acteurs;
    }

    /**
     * Instance a new InfoFilm from its constructor
     * giving all the accessors to the data in parameter
     * which it need from the id of the film
     * @param id The id_film
     * @return The information over the film
     * @author Dray Raphael
     * @see InfoFilm
     */
    private InfoFilm getInfoFilm(int id) {
        return new InfoFilm(getTitre(id),
                            getRealisateurs(id),
                            getActeurs(id),
                            getPays(id),
                            getAnnee(id),
                            getDuree(id),
                            getAutresTitres(id));
    }

    /**
     * Check if a String is a keyword
     * @param s The string to test
     * @return A boolean to the question
     * @author Dray Raphael
     * @see KeyWord
     */
    private boolean isKeyword(String s) {
        for(KeyWord k : KeyWord.values()) if (k.getName().equals(s)) return true;
        return false;
    }

}
