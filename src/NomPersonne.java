/**
 *    Management of the names (family names + first name) of the persons.
 *    <p>
 *    The NomPersonne class allow to generate the names and consider
 *    prefixes of the names ('de', 'von', 'van')
 *    in the sort.
 */ 
public class NomPersonne implements Comparable<NomPersonne>{
    private String _nom;
    private String _prenom;
    private int    _debutComp;

    /**
     *    Create a new NomPersonne. Attention, the first name
     *    is given in second.
     *
     *    @param nom Family name or artist name
     *    @param prenom First name (can be null)
     */
    public NomPersonne(String nom, String prenom) {
        _nom = nom;
        _prenom = prenom;
        _debutComp = 0;
        // We look at the first character in CAPS
        // to sort 'von Stroheim' with the S, 'de la Huerta'
        // with the H and 'de Funès' with the F.
        // 'De Niro' will be with the D.
        while ((_debutComp < _nom.length())
               && (_nom.charAt(_debutComp)
                   == Character.toLowerCase(_nom.charAt(_debutComp)))) {
           _debutComp++;
        }
    }

    /**   Comparator that consider names prefixes.
     *    
     *    @param autre NomPersonne that is compared to the current object
     *    @return an negative, equal or positive Integer
     */
    @Override
    public int compareTo(NomPersonne autre) {
        if (autre == null) {
          return 1;
        }
        int cmp = this._nom.substring(this._debutComp)
                      .compareTo(autre._nom.substring(autre._debutComp));
        if (cmp == 0) {
          return this._prenom.compareTo(autre._prenom);
        } else {
          return cmp;
        }
    }
    
    /**
     *   Return a printable name.
     *   <p>
     *   If there is a mention such as (Jr.) which in the base is in
     *   the column of the first names, it is postponed to the end.
     *   @return The combination of the first name and the name.
     */
    @Override
    public String toString() {
        int pos = -1;

        if (this._prenom != null) {
          // The mention in parentheses will be send
          // to the end.
          pos = this._prenom.indexOf('(');
        }
        if (pos == -1) {
          if (this._prenom == null) {
            return this._nom;
          } else {
            return this._prenom + " " + this._nom;
          }
        } else {
          return this._prenom.substring(0, pos-1).trim() 
                 + " " + this._nom
                 + " " + this._prenom.substring(pos).trim();
        }
    }
}
