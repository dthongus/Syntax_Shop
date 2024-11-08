// Modul mit einer Klasse für die Hauptkategorieliste

import java.sql.Connection // Bibliothek zur Interaktion zwischen Programm und Datenbank
import java.sql.DriverManager // Klasse zur Herstellung der Datenbankverbindung


// Klasse für die Erstellung der Produktliste in der Hauptkategorie
class MainCategory(val name: String) {

    companion object {

        // Funktion zur Erstellung einer Liste die mit Daten aus der Datenbank befüllt wird
        fun getAllCategories(): List<MainCategory> {

            // Liste für Hauptkategorie erstellen
            val categories = mutableListOf<MainCategory>()

            // Datenbankverbindung herstellen
            val connection: Connection = DriverManager.getConnection("jdbc:sqlite:Database.db")
            val query = "SELECT name FROM mainCategory"
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(query)

            // Holt Daten zur Hauptkategorie, solange Daten in der Tabelle vorhanden sind und fügt diese in die Liste hinzu.
            while (resultSet.next()) {
                val name = resultSet.getString("name")
                categories.add(MainCategory(name))
            }
            // Datenbank schließen
            resultSet.close()
            statement.close()
            connection.close()

            return categories
        }
    }
}
