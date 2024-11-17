// Main mit 4 Menüfunktionen
// main() -> Aufruf des Hauptmenüs
// mainMenue() -> Anzeige des Hauptmenüs
// login() -> Anzeige des Login Menüs
// adminMenue() -> Anzeige des Admin Menüs
// customerMenue() -> Anzeige des Kunden Menüs


import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet


// Funktion zum Aufrufen des Hauptmenüs und erstellen einer Datenbank mit Datenbefüllung
fun main() {
    // Datenbank erstellen und mit Produktdaten für einen ersten Bestand befüllen
    createDatabase()
    // Aufruf des Hauptmenüs
    mainMenue()
}


// Aufruf des Hauptmenüs
fun mainMenue() {
    println("\n### Willkommen im PC-Store© Germany ###")
    println("\n### Hauptmenü ###")
    println("[1] Anmelden\n[2] Account erstellen\n[3] Beenden")
    println("\nBitte Auswahl treffen:")

    // Nutzer Eingabe für Auswahl aus Menü
    val userinput = safeExecute {
        val input = readln()
        val validInput = input.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${mainMenue()}")
        validInput
    }

    // Nutzereingabe wird an die jeweilige Funktion übergeben
    when (userinput.toInt()) {
        1 -> login()  // Aufruf des Login Menüs
        2 -> addAccount() // Aufruf des Account Menüs zum Anlegen eines neuen Accounts
        3 -> return println("Auf wiedersehen und Danke für den Besuch!") // Beendet das Programm
        else -> {  // Falls Eingabe des Nutzers nicht in der Auswahlliste vorhanden ist
            println("Eingabe nicht erkannt! Bitte erneut eingeben:") // Fehlermeldung
            mainMenue()  // Ruft bei Fehleingabe des Nutzers die Funktion (rekursiv) erneut auf.
        }
    }
}


// Aufruf des Login Menüs
fun login() {
    println("\n### Anmeldung ###")
    println("\nGeben Sie Ihren Benutzernamen ein:")
    val username = readln()
    println("Geben Sie Ihr Passwort ein:")
    val password = readln()

    // Verbindung zur Datenbank aufbauen
    val url = "jdbc:sqlite:Database.db"
    val connection = DriverManager.getConnection(url)

    // Warenkorb Daten leeren um aktuellen eingeloggten Nutzer einzufügen
    val deleteQuery = "DELETE FROM shoppingCard"
    val deleteStatement = connection.createStatement()
    deleteStatement.executeUpdate(deleteQuery)

    // Prüfen, ob die Eingaben in der Datenbank vorhanden sind
    val query = "SELECT * FROM accounts WHERE userName = ? AND userPassword = ?"
    val statement: PreparedStatement = connection.prepareStatement(query)
    statement.setString(1, username)
    statement.setString(2, password)

    // Ergebnis speichern
    val resultSet: ResultSet = statement.executeQuery()

    // Abfrage ob Nutzerdaten in der Datenbank vorhanden sind
    if (resultSet.next()) {
        println("\nAnmeldung erfolgreich! Herzlich Willkommen $username")

        val checkAllocation = "SELECT allocation FROM accounts WHERE userName = ?"
        val allocationStatement = connection.prepareStatement(checkAllocation)
        allocationStatement.setString(1, username)
        val allocationResultSet = allocationStatement.executeQuery()

        // Überprüfen, ob das ResultSet ein Ergebnis enthält
        if (allocationResultSet.next()) {
            // Abrufen des Wertes der Spalte "allocation" für den gefundenen Eintrag
            val allocation = allocationResultSet.getInt("allocation")

            // Prüfen, ob ein Kunde oder Admin sich eingeloggt hat
            if (allocation == 2) {
                // Falls sich ein Admin angemeldet hat
                deleteStatement.close()
                connection.close()
                // Aufruf des Admin Menüs
                adminMenue()
            // Falls sich ein Kunde angemeldet hat
            } else {
                // Einfügen der Nutzerdaten in den Warenkorb (shoppingCard)
                val insertProducts = "INSERT INTO shoppingCard (user, paymentMethod) VALUES (?, ?)"
                val productStatement: PreparedStatement = connection.prepareStatement(insertProducts)

                // Zahlungsmethode anhand des Benutzernamens auslesen und in Warenkorb einfügen
                val paymentMethodQuery = "SELECT paymentMethod FROM accounts WHERE userName = ?"
                val paymentMethodStatement: PreparedStatement = connection.prepareStatement(paymentMethodQuery)
                paymentMethodStatement.setString(1, username)

                // Ausführen der Abfrage und Abrufen des paymentMethod Wert
                val paymentMethodResult: ResultSet = paymentMethodStatement.executeQuery()
                if (paymentMethodResult.next()) {
                    val paymentMethod = paymentMethodResult.getString("paymentMethod")

                    // Werte im PreparedStatement einfügen
                    productStatement.setString(1, username)
                    productStatement.setString(2, paymentMethod)

                    // Ausführungsbefehl
                    productStatement.executeUpdate()

                    // Schließen der Statement-Objekte und Verbindung
                    productStatement.close()
                    paymentMethodStatement.close()
                    deleteStatement.close()
                    connection.close()

                    // Kunden Menü aufrufen
                    customerMenue()
                }
            }
        }
        allocationResultSet.close()
        allocationStatement.close()
    //  Falls keine Nutzerdaten vorhanden sind, dann gib eine Fehlermeldung und rufe das Login Menü erneut auf
    } else {
        println("\nBenutzername oder Passwort ist falsch! Bitte erneut versuchen!")
        resultSet.close()
        statement.close()
        connection.close()
        //  Login Menü erneut aufrufen
        login()
    }
}


// Aufruf des Adminmenü
fun adminMenue() {
    println("\n### Admin Menü ###")
    println("[1] Produkt hinzufügen\n[2] Produkt entfernen\n[3] Produktbestand auffüllen\n" +
            "[4] Accountliste\n[5] Beenden")
    println("Bitte Auswahl treffen:")

    // Nutzer Eingabe für Auswahl aus Menü
    val adminInput = safeExecute {
        val input = readln()
        val validInput = input.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${adminMenue()}")
        validInput
    }
    // Nutzereingabe wird an die jeweilige Funktion übergeben
    when (adminInput) {
        1 -> addProducts()
        2 -> delProduct()
        3 -> orderProduct()
        4 -> showAccounts()
        5 -> {
            println("Auf wiedersehen!")
            return
        }
        // Bei falscher Eingabe, gib Fehlermeldung aus und ruf das Admin Menü erneut auf
        else -> println("Eingabe nicht erkannt! Bitte erneut versuchen: ${adminMenue()}")
    }
}


// Aufruf des Kundenmenü
fun customerMenue() {
    println("\n### Mein Kunden Menü ###")
    println("[1] Mein Warenkorb\n[2] zum Shop\n[3] Alle Produkte\n[4] Produkt bewerten")
    println("Bitte Auswahl eingeben:")

    // Nutzer Eingabe für Auswahl aus Menü
    val userInputCustom = safeExecute {
        val input = readln()
        val validInput = input.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${customerMenue()}")
        validInput
    }
    // Nutzereingabe wird an die jeweilige Funktion übergeben
    when (userInputCustom) {
        1 -> showCard()
        2 -> produktList()
        3 -> totalProductList()
        4 -> addReviews()
        // Bei falscher Eingabe, gib Fehlermeldung aus und ruf das Kunden Menü erneut auf
        else -> {
            println("Eingabe nicht erkannt! Bitte erneut eingeben.")
            customerMenue()
        }
    }

    // Ressourcen schließen
    statement.close()
    connection.close()
}


