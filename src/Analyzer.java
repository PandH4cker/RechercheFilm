import java.util.LinkedList;

/**
 * Class that will analyze a given String and create Requests from it.
 * Will order those Requests in a certain manner inside a LinkedList<LinkedList<String>>.
 * {[Request1][Request2][Request3]}
 * {[Request4]}
 * {[Request5]}
 * This is a representation of how the Requests are classified inside the list.
 * Request 1, 2 and 3 are bound by an "or" relation.
 * Request 4 and 5 are independent (Request 4 and 5 are bound by an "and" relation,
 * the group containing request 1, 2 and 3 is also bound by an "and" relation with request 4 and request 5).
 * The class also handles the eventual errors from the given String analysis.
 *
 * @author  Dray Raphael
 * @version 1.0
 */
public class Analyzer {
    private LinkedList<LinkedList<String>> requestedList;
    private String s;


    /**
     * Simple constructor that will initialize the requestedList.
     *
     * @author  Dray Raphael
     * @param s the String to analyze
     * @see LinkedList
     */
    public Analyzer(String s) {
        this.requestedList = new LinkedList<>();
        this.s = s;
        this.analyzeInput(this.s);
    }

    public String getS() {
        return s;
    }

    /**
     * Function that will analyze and create requests from the String given in parameter.
     *
     * @author  Dray Raphael
     * @param s String written by the user that will be analyzed.
     * @throws InvalidRequestException if request seems to be null
     */
    private void analyze(String s) throws InvalidRequestException {

        if (s.isEmpty()){
            throw new InvalidRequestException("string is empty!");
        }

        KeyWord lastKeyWord = null;
        boolean isOrRequest = false;
        Request requestTemp = null;

        String tempString;
        LinkedList<Integer> indexList;
        requestedList = new LinkedList<>();

        String[] splittedString = s.split(" ");
        String[] splittedStringLowerCase = s.toLowerCase().split(" ");

        int i = 0;
        while (i < splittedStringLowerCase.length) {

            tempString = splittedStringLowerCase[i];
            indexList = containsPunct(tempString);
            if (indexList.getLast() != -1)
                for (Integer integer : indexList)
                    tempString = charRemoveAt(tempString, integer);

            assert tempString != null;
            switch (tempString) {
                case "titre":
                    lastKeyWord = KeyWord.TITRE;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "de":
                    lastKeyWord = KeyWord.REALISATEUR;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "avec":
                    lastKeyWord = KeyWord.ACTEUR;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "pays":
                    lastKeyWord = KeyWord.PAYS;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "en":
                    lastKeyWord = KeyWord.DATE;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "avant":
                    lastKeyWord = KeyWord.AVANT;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "apres":
                    lastKeyWord = KeyWord.APRES;
                    addRequest(requestTemp);
                    requestTemp = new Request(lastKeyWord);
                    requestTemp.setIsOrRequest(isOrRequest);
                    isOrRequest = false;
                    break;

                case "ou":
                    isOrRequest = true;
                    addRequest(requestTemp);
                    requestTemp = null;
                    break;

                default:
                    if (null == requestTemp)
                        if (null == lastKeyWord) throw new InvalidRequestException("No KeyWord for request");
                        else {
                            requestTemp = new Request(lastKeyWord);
                            requestTemp.addVal(splittedString[i]);
                            requestTemp.setIsOrRequest(isOrRequest);
                            isOrRequest = false;
                        }
                    else if (requestTemp.containsPunct()) {
                        addRequest(requestTemp);
                        requestTemp = new Request(lastKeyWord);
                        requestTemp.addVal(splittedString[i]);
                        requestTemp.setIsOrRequest(isOrRequest);
                        isOrRequest = false;
                    } else requestTemp.addVal(splittedString[i]);
                    break;
            }
            i++;
        }
        addRequest(requestTemp);
    }

    /**
     * Function that will add the given Request to the requestedList
     * Will check if the Request isn't null.
     * Then, whether or not the Request is an "or" request,
     * will add that Request at the correct place in the requestedList.
     *
     * @author  Dray Raphael
     * @param request Request that must be added to the requestedList
     * @throws InvalidRequestException if request seems to be null
     */
    private void addRequest(Request request) throws InvalidRequestException {
        if (request == null) return;
        if (!request.getIsOrRequest()) {
            this.requestedList.addLast(new LinkedList<>());
            this.requestedList.getLast().addLast(request.finishRequest());
        } else this.requestedList.getLast().addLast(request.finishRequest());
    }

    /**
     * Simple get function that will return the requestedList.
     * LinkedList<String> contains individual requests that are bound together with an "or" relation.
     * While LinkedList<LinkedList<String>> contains those groups of requests.
     *
     * @author  Dray Raphael
     * @return return a LinkedList<LinkedList<String>> containing all the individual requests.
     */
    public LinkedList<LinkedList<String>> getRequestedList(){
        return this.requestedList;
    }

    /**
     * Function that will check wether or not val contains punctuation {",",";",".",":"}.
     * Then will return a list of Integer pointing to the indexes of punctuation characters if any.
     *
     * @author  Dray Raphael
     * @param val String to remove the punctuation
     * @return return a LinkedList<Integer> containing the indexes of punctuation characters, -1 if there are none.
     */
    public LinkedList<Integer> containsPunct(String val){

        String[] punct = {",", ";", ".", ":"};
        LinkedList<Integer> list = new LinkedList<>();
        boolean containsPunct = false;

        if (val != null)
            for (String value : punct)
                if (val.contains(value)) {
                    containsPunct = true;
                    break;
                }

        if (!containsPunct) list.addLast(-1);
        else {
            int i = 0;
            while (i < val.length()) {
                for (String value : punct)
                    if (val.charAt(i) == value.charAt(0))
                        list.addLast(i);
                i++;
            }
        }

        return list;
    }

    /**
     * Function that will remove from the given String a character at the given index.
     *
     * @author  Dray Raphael
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
     * Function that will call analyze() and forward the results if no error happened.
     * Will forward the errors encountered instead of the result.
     *
     * @author  Dray Raphael
     * @param inputString String that will be used as parameter in the analyze() call.
     */
    public void analyzeInput(String inputString) {
        LinkedList<LinkedList<String>> temp;
        try{
            analyze(inputString);
        }catch (InvalidRequestException e){
            temp = new LinkedList<>();
            temp.addLast(new LinkedList<>());
            temp.getLast().addLast("ERROR;"+e.getMessage());
        }
    }
}