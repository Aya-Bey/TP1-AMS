package presentation;

import dao.DaoIMP;
import dao.IDao;
import metier.MetierIMP;
import metier.IMetier;

public class PresentationStatique {
    public static void main(String[] args) {
        IDao dao = new DaoIMP();
        IMetier metier = new MetierIMP();

        ((MetierIMP) metier).setDao(dao);

        System.out.println(metier.calcul());
    }
}