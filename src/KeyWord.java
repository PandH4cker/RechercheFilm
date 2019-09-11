/**
 * Simple enum class to implements the differents KeyWords
 *
 * @author  Dray Raphael
 * @version 1.0
 */
public enum KeyWord {
    TITRE("TITRE"),
    REALISATEUR("DE"),
    ACTEUR("AVEC"),
    PAYS("PAYS"),
    DATE("EN"),
    AVANT("AVANT"),
    APRES("APRES");

    private String name;

    /**
     * Basic constructor for this enum class.
     *
     * @author  Dray Raphael
     * @param name String value of the name of the enum.
     */
    KeyWord(String name){
        this.name = name;
    }

    /**
     * Simple toString function, allowing the return a String val of the enum.
     *
     * @author  Dray Raphael
     * @return return a String containing the name.
     */
    public String toString(){
        return this.name;
    }

    /**
     *  Name attribute accessor
     * @return String name attribute
     */
    public String getName() {
        return this.name;
    }

}