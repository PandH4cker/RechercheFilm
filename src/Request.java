/**
 * Class that represents individual requests in our search engine.
 * This class handles all the internal treatments needed for each requests.
 *
 * @author  Raphael Dray
 * @version 1.0
 */
public class Request {
    private String string;
    private KeyWord keyWord;
    private boolean isOrRequest;

    /**
     * Simple constructor that will initialize the KeyWord and the "or" flag of the request.
     *
     * @author  Raphael Dray
     * @param keyWord the KeyWord of the class.
     */
    public Request(KeyWord keyWord){
        this.keyWord = keyWord;
        this.isOrRequest = false;
    }

    /**
     * Simple get function to return the KeyWord of the request
     *
     * @author  Raphael Dray
     * @return return the KeyWord of the request
     */
    public KeyWord getKeyWord() {
        return keyWord;
    }

    /**
     * Simple get function to return the String value of the request
     *
     * @author  Raphael Dray
     * @return return the String value of the request
     */
    public String getString() {
        return string;
    }

    /**
     * Simple get function to return the "or" flag of the request
     *
     * @author  Raphael Dray
     * @return return the "or" flag of the request
     */
    public boolean getIsOrRequest(){
        return isOrRequest;
    }

    /**
     * Simple set function to set the "or" flag of the request
     *
     * @author  Raphael Dray
     * @param val a boolean
     */
    public void setIsOrRequest(boolean val){
        this.isOrRequest = val;
    }

    /**
     * Simple set function to set the KeyWord of the request
     *
     * @author  Raphael Dray
     * @param keyWord un KeyWord
     */
    private void setKeyWord(KeyWord keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Simple set function to set the String value of the request
     *
     * @author  Raphael Dray
     * @param string a String
     */
    private void setString(String string) {
        this.string = string;
    }

    /**
     * Function that will add the given String value to the current String value of the request.
     * Will check if string is not null first.
     *
     * @author  Raphael Dray
     * @param val a String
     */
    public void addVal(String val){
        if (getString() != null) setString(getString() + val + " ");
        else setString(val + " ");
    }

    /**
     * Function that will finalize the request.
     * Will first clean the request String value, removing unneeded punctuation and blank spaces.
     * Will then check if the request is valid, ie: String value isn't empty and is coherent with the KeyWord.
     * If all is correct, will return the finalized request.
     *
     * @author  Raphael Dray
     * @return return a String value containing the finalized request with a KeyWord followed by the value.
     * @throws InvalidRequestException if request seems to be null
     */
    public String finishRequest() throws InvalidRequestException {
        cleanStr();
        requestIsValid();
        return String.format("%s;%s", getKeyWord().toString(), getString());
    }

    /**
     * Function that will check wether or not string contains punctuation {",",";",".",":"}.
     *
     * @author  Raphael Dray
     * @return return a boolean, true if string contains punctuation, false if not.
     */
    public boolean containsPunct(){
        String[] punct = {",", ";", ".", ":"};
        if (this.getString() != null) for (String s : punct) if (this.getString().contains(s)) return true;
        return false;
    }

    /**
     * Function that will "clean" string.
     * Removing unneeded punctuation and blank spaces.
     *
     * @author  Dray Raphael
     */
    private void cleanStr() {
        String[] punct = {",", ";", ".", ":"};
        String temp;
        int index;

        if (containsPunct()) do for (String s : punct) {
            index = string.indexOf(s);
            if (index != -1) {
                temp = charRemoveAt(string, index);
                if (temp != null) string = temp;
            }
        } while (containsPunct());

        if (string.charAt(string.length() - 1) == ' ') do {
            temp = charRemoveAt(string, string.length() - 1);
            if (temp != null) string = temp;
        } while (string.charAt(string.length() - 1) == ' ');
    }

    /**
     * Function that will remove from the given String a character at the given index.
     *
     * @author  Raphael Dray
     * @param p index of the character we want removed.
     * @param str String in which we want to remove a character.
     * @return return the String given as parameter but with the character at the given index removed.
     */
    private static String charRemoveAt(String str, int p) {
        if ((null != str) && (p >= 0) && (p < str.length()))
            return String.format("%s%s", str.substring(0, p), str.substring(p + 1));
        return null;
    }

    /**
     * Function that will check if the request is valid or not.
     * ie: String value isn't empty and is coherent with the KeyWord.
     * If the request is not valid, will throw an exception. If not exception are thrown, request is valid.
     *
     * @author  Raphael Dray
     * @throws InvalidRequestException if request seems to be null
     */
    private void requestIsValid() throws InvalidRequestException {
        if (!((getString() != null) && !(getString().equals(""))))
            throw new InvalidRequestException(String.format("Request %s is empty", getKeyWord().toString()));
        if  (getKeyWord().toString().equals("EN") || (getKeyWord().toString().equals("AVANT")) || (getKeyWord().toString().equals("APRES"))) {
            if (!getString().matches("[0-9]+"))
                throw new InvalidRequestException(String.format("Request %s does not contain only numbers", getKeyWord().toString()));
        } else {
            int i = 0;
            while (i < getString().length()) {
                if ((!(Character.isLetter(getString().charAt(i)))) && (getString().charAt(i) != ' '))
                    throw new InvalidRequestException(String.format("Request %s does not contain only letters", getKeyWord().toString()));
                i++;
            }
        }
    }
}