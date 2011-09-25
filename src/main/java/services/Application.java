package services;

import static framework.GlobalHelpers.*;

public class Application {

    public void start() throws Exception {
        System.out.println("Starting...");

        // routing
        match("").rewrite("/sample/index");
        match("articles/$category/$region").rewrite("articles/list");
        match("$controller/$action(/$id)");

        // uwagi - gdy nie ma matchow to jest jeden match
        // $controller/$action(/$id)
        // algorytm - petla po wszystkich wyrazeniach
        // algorytm - dopasowywanie... znak po znaku, chyba ¿e $-wtedy czytaj
        // znaki zmiennej Java

        // maszyna stanow

        // 0 - zwykly znak
        // 1 - zmienna
    }

    public void stop() {
        System.out.println("Stopping...");
    }

}
