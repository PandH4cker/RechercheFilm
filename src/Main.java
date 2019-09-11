public class Main {
    public static void main(String[] args) {
        RechercheFilm rF = new RechercheFilm("SQLiteSample/bdfilm.sqlite");
        System.out.print(rF.retrouve("DE Hitchcock"));
        rF.fermeBase();
    }
}