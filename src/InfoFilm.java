import java.util.ArrayList;
import java.util.Collections;

/**
 *   Synthetic informations over a film.
 */
public class InfoFilm implements Comparable<InfoFilm> {
    private String                 _titre;
    private ArrayList<NomPersonne> _realisateurs;
    private ArrayList<NomPersonne> _acteurs;
    private String                 _pays;
    private int                    _annee;
    private int                    _duree;
    private ArrayList<String>      _autres_titres;

    /**
     *  Constructor.
     *
     *  @param titre The title (in french generally) of the film
     *  @param realisateurs The list of the directors (can be null)
     *  @param acteurs The list of the actors (can be null)
     *  @param pays The Name of the country (in french)
     *  @param annee Release date
     *  @param duree Time in minutes; 0 or a negative value if the information is not known
     *  @param autres_titres The list of the alternative titles  (can be null), original title or english title or other
     */
    public InfoFilm(String titre,
                    ArrayList<NomPersonne> realisateurs,
                    ArrayList<NomPersonne> acteurs,
                    String pays,
                    int annee,
                    int duree,
                    ArrayList<String> autres_titres) {
       _titre = titre;
       _realisateurs = realisateurs;
       Collections.sort(_realisateurs);
       _acteurs = acteurs;
       Collections.sort(_acteurs);
       _pays = pays;
       _annee = annee;
       _duree = duree;
       _autres_titres = autres_titres;
       Collections.sort(_autres_titres);
    }

    /**
     *   Compare by title, then year and finally by country.
     *
     *    @return A negative, equal or positive integer 0 in other cases
     */
    @Override
    public int compareTo(InfoFilm autre) {
       if (autre == null) {
         return 1;
       }
       int cmp = this._titre.compareTo(autre._titre);
       if (cmp == 0) {
         cmp = (Integer.compare(this._annee, autre._annee));
         if (cmp == 0) {
           cmp = this._pays.compareTo(autre._pays);
         }
       }
       return cmp;
    }

    /**
     *   Print like a JSON Object informations over a film.
     *   <p>
     *   Directors and actors are sort by alphabetical order, the time is cast in hours and minutes.
     *
     *   @return String formatted like a JSON Object.
     */
    @Override
    public String toString() {
        boolean debut = true;
        StringBuilder sb = new StringBuilder();
        sb.append("{\"titre\":\"").append(_titre.replace("\"", "\\\"")).append("\",");
        sb.append("\"realisateurs\":[");
        for (NomPersonne nom: _realisateurs) {
           if (debut) {
             debut = false;
           } else {
             sb.append(',');
           }
           sb.append("\"").append(nom.toString().replace("\"", "\\\"")).append("\"");
        }
        sb.append("],\"acteurs\":[");
        debut = true;
        for (NomPersonne nom: _acteurs) {
           if (debut) {
             debut = false;
           } else {
             sb.append(',');
           }
           sb.append("\"").append(nom.toString().replace("\"", "\\\"")).append("\"");
        }
        sb.append("],\"pays\":\"");
        sb.append(_pays.replace("\"", "\\\""));
        sb.append("\",\"annee\":");
        sb.append(_annee);
        sb.append(",\"duree\":");
        if (_duree > 0) {
          sb.append('"');
          int h = _duree / 60;
          sb.append(h).append("h");
          int mn = _duree % 60;
          if (mn > 0) {
            sb.append(mn).append("mn");
          }
          sb.append('"');
        } else {
          sb.append("null");
        }
        sb.append(",\"autres titres\":[");
        debut = true;
        for (String titre: _autres_titres) {
           if (debut) {
             debut = false;
           } else {
             sb.append(',');
           }
           sb.append("\"").append(titre.replace("\"", "\\\"")).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }
}
