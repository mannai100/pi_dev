package tests;

import entities.Personne;
import services.ServicePersonne;
import utils.MyDatabase;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args)  {
        Personne personne = new Personne(1,"hello","3a63",17);
        ServicePersonne servicePersonne = new ServicePersonne();
        try {
            System.out.println(servicePersonne.afficher());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
