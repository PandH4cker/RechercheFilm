/**
 * Simple class implementing Throwable for the errors in Analyzer.java
 *
 * @author  Dray Raphael
 * @version 1.0
 */
public class InvalidRequestException extends Throwable {
    InvalidRequestException(String message){
        super(message);
    }
}