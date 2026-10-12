# TRAVAIL PRATIQUE N°1

**Matière :** Architecture MicroServices  
**Filière :** MPSRCC1  
**Réalisé par :** Aya Bey  
**Année universitaire :** 2026–2027

---

# TP1 — Inversion de Contrôle et Injection de Dépendances

## 1. Vérification du fichier `pom.xml`

### Présentation

Le fichier `pom.xml` est le fichier de configuration du projet Maven. Il permet de déclarer les dépendances nécessaires à la compilation et à l’exécution de l’application, notamment celles du framework Spring.


![Vérification du fichier pom.xml](images/pom.xml.png)

---

## 2. Présentation du TP

Ce travail pratique porte sur les principes de l’**Inversion de Contrôle (IoC)** et de l’**Injection de Dépendances (DI)** dans une application Java.

Les quatre approches étudiées sont l’injection statique, l’injection dynamique par réflexion, la configuration Spring XML et l’injection par annotations Spring.

## 3. Objectifs pédagogiques

- Comprendre le rôle des interfaces et le principe du faible couplage.
- Mettre en œuvre différentes méthodes d’injection de dépendances.
- Comprendre l’Inversion de Contrôle.
- Découvrir la gestion des composants avec Spring.
- Comparer les approches étudiées.

## 4. Structure du projet

Le projet est organisé en trois packages principaux :

- `dao` : accès aux données.
- `metier` : logique métier.
- `presentation` : lancement et test des différentes approches.

Le dossier `src/main/resources` contient les fichiers de configuration. Le dossier `images` contient les cinq captures d’écran du projet.

![Structure du projet](images/la structure.png)

---

## 5. Réalisation des activités

### Activité 1-1 : Injection de dépendances par instanciation statique

#### Principe

La dépendance est créée et injectée manuellement dans la classe de présentation. La classe métier dépend de l’interface `IDao`, et non directement d’une implémentation concrète.

#### Étape 1 : Interface `IDao` — package `dao`

```java
package dao;

public interface IDao {
    double getValue();
}
```

#### Étape 2 : Classe `DaoIMP` — package `dao`

```java
package dao;

public class DaoIMP implements IDao {
    @Override
    public double getValue() {
        return 3;
    }
}
```

#### Étape 3 : Interface `IMetier` — package `metier`

```java
package metier;

public interface IMetier {
    double calcul();
}
```

#### Étape 4 : Classe `MetierIMP` — package `metier`

```java
package metier;

import dao.IDao;

public class MetierIMP implements IMetier {
    private IDao dao;

    @Override
    public double calcul() {
        return dao.getValue() * 7;
    }

    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
```

#### Étape 5 : Classe `PresentationStatique` — package `presentation`

```java
package presentation;

import dao.DaoIMP;
import dao.IDao;
import metier.IMetier;
import metier.MetierIMP;

public class PresentationStatique {
    public static void main(String[] args) {
        IDao dao = new DaoIMP();
        MetierIMP metier = new MetierIMP();

        metier.setDao(dao);

        System.out.println(metier.calcul());
    }
}
```

#### Réponses aux questions

**Question 9 — Pourquoi le couplage est-il faible ?**

`MetierIMP` dépend de l’interface `IDao` plutôt que de la classe concrète `DaoIMP`. Il est ainsi possible de remplacer l’implémentation du DAO sans modifier la logique métier.

**Question 10 — Le principe IoC est-il respecté ?**

Il n’est que partiellement respecté : la dépendance est injectée de l’extérieur de `MetierIMP`, mais la classe de présentation crée elle-même les objets.

**Question 11 — Comment éviter que la présentation contrôle la création des objets ?**

La création et l’assemblage des objets peuvent être confiés à un conteneur IoC comme Spring.

#### Résultat d’exécution

```text
21.0
```

![Activité 1-1 : injection statique](images/activité-1-1.png)

---

### Activité 1-2 : Injection de dépendances par instanciation dynamique

#### Principe

Les noms des classes sont définis dans `config.txt`. La réflexion Java permet de charger les classes et de créer les objets dynamiquement.

#### Étape 1 : Fichier `config.txt` — dossier `src/main/resources`

```text
dao.DaoIMP
metier.MetierIMP
```

#### Étape 2 : Classe `PresentationDynamique` — package `presentation`

```java
package presentation;

import dao.IDao;
import metier.IMetier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class PresentationDynamique {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                PresentationDynamique.class
                    .getClassLoader()
                    .getResourceAsStream("config.txt")
            )
        );

        String daoClassName = br.readLine();
        String metierClassName = br.readLine();
        br.close();

        Class<?> cDao = Class.forName(daoClassName);
        IDao dao = (IDao) cDao.getDeclaredConstructor().newInstance();

        Class<?> cMetier = Class.forName(metierClassName);
        IMetier metier =
            (IMetier) cMetier.getDeclaredConstructor().newInstance();

        Method setDao = cMetier.getMethod("setDao", IDao.class);
        setDao.invoke(metier, dao);

        System.out.println(metier.calcul());
    }
}
```

#### Réponses aux questions

**Question 7 — Comment fonctionne l’instanciation dynamique ?**

Les noms des classes sont lus dans `config.txt`, puis les classes sont chargées avec `Class.forName()` et instanciées par réflexion. L’injection est réalisée en appelant le setter par réflexion.

**Question 8 — En quoi cette approche se rapproche-t-elle de l’IoC ?**

Le choix des implémentations est externalisé dans un fichier de configuration. La classe de présentation n’a donc plus besoin de nommer directement les classes concrètes, même si elle gère encore la création et l’injection.

**Question 9 — Quels sont les avantages et les limites ?**

Cette méthode améliore la flexibilité et permet de changer la configuration sans modifier le code de présentation. En revanche, la réflexion rend le code plus complexe et certaines erreurs ne sont détectées qu’à l’exécution.

#### Résultat d’exécution

```text
21.0
```

![Activité 1-2 : injection dynamique](images/activité-1-2.png)

---

### Activité 1-3 : Injection de dépendances avec Spring XML

#### Principe

Le fichier `config.xml` déclare les beans et leurs dépendances. Le conteneur Spring crée les objets et injecte la dépendance du DAO dans le composant métier.

#### Étape 1 : Fichier `config.xml` — dossier `src/main/resources`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="dao" class="dao.DaoIMP"/>

    <bean id="metier" class="metier.MetierIMP">
        <property name="dao" ref="dao"/>
    </bean>

</beans>
```

#### Étape 2 : Classe `PresentationAvecSpring` — package `presentation`

```java
package presentation;

import metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PresentationAvecSpring {
    public static void main(String[] args) {
        ApplicationContext context =
            new ClassPathXmlApplicationContext("config.xml");

        IMetier metier = context.getBean("metier", IMetier.class);
        System.out.println(metier.calcul());
    }
}
```

#### Réponses aux questions

**Question 6 — Comment Spring gère-t-il les dépendances ?**

Spring lit `config.xml`, crée les beans déclarés et injecte le bean `dao` dans le bean `metier` grâce à la propriété `dao`. La création et l’assemblage des objets sont confiés au conteneur : cela illustre l’Inversion de Contrôle.

**Question 7 — Comparaison des méthodes**

- **Création manuelle :** le programmeur crée et assemble les objets dans le code.
- **Réflexion :** les classes sont sélectionnées depuis un fichier et instanciées dynamiquement.
- **Spring :** le conteneur crée les objets et gère leurs dépendances à partir de la configuration.

#### Résultat d’exécution

```text
21.0
```

![Activité 1-3 : injection Spring XML](images/activité-1-3.png)

---

### Activité 1-4 : Injection de dépendances par annotations Spring

#### Principe

Les annotations Spring permettent d’identifier les composants directement dans le code Java. Le contexte Spring détecte les composants et gère l’injection de leurs dépendances.

#### Étape 1 : Classe `DaoIMP` — package `dao`

```java
package dao;

import org.springframework.stereotype.Repository;

@Repository
public class DaoIMP implements IDao {
    @Override
    public double getValue() {
        return 3;
    }
}
```

#### Étape 2 : Classe `MetierIMP` — package `metier`

```java
package metier;

import dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetierIMP implements IMetier {
    @Autowired
    private IDao dao;

    @Override
    public double calcul() {
        return dao.getValue() * 7;
    }

    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
```

#### Étape 3 : Classe `PresentationAvecAnnotation` — package `presentation`

```java
package presentation;

import metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class PresentationAvecAnnotation {
    public static void main(String[] args) {
        ApplicationContext context =
            new AnnotationConfigApplicationContext("dao", "metier");

        IMetier metier = context.getBean(IMetier.class);
        System.out.println(metier.calcul());
    }
}
```

#### Réponses aux questions

**Question 5 — Quel est le rôle des annotations ?**

- `@Repository` identifie le composant d’accès aux données.
- `@Service` identifie le composant métier.
- `@Autowired` demande à Spring d’injecter automatiquement une dépendance compatible avec le type `IDao`.

**Question 6 — Pourquoi cette approche est-elle plus maintenable et comment illustre-t-elle l’IoC ?**

Les annotations réduisent le besoin de déclarer les beans dans un fichier XML. Spring détecte les composants et gère leur création ainsi que leur assemblage : le contrôle est confié au conteneur.

#### Résultat d’exécution

```text
21.0
```

![Activité 1-4 : injection par annotations](images/activité-1-4.png)

---

## 6. Comparaison des approches

| Approche | Qui gère la création et l’assemblage ? | Mécanisme |
|---|---|---|
| Injection statique | Le programmeur | `new` et setter |
| Injection dynamique | Le programme, à partir de la configuration | `config.txt` et réflexion |
| Spring XML | Le conteneur Spring | `config.xml` |
| Annotations Spring | Le conteneur Spring | Annotations et scan des composants |

## 7. Résultats obtenus

Le résultat attendu pour les quatre approches est :

```text
21.0
```

Il correspond au calcul `3 × 7`, réalisé par la couche métier à partir de la valeur fournie par le DAO.

## 8. Conclusion générale

Ce TP a permis d’étudier quatre approches d’injection de dépendances en Java. Les interfaces favorisent le faible couplage, tandis que la configuration dynamique et Spring offrent une gestion plus flexible des objets. Avec Spring XML ou les annotations, le conteneur prend en charge la création et l’assemblage des composants, illustrant le principe d’Inversion de Contrôle.
