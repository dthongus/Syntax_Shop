// Modul mit einer Klasse für Unterkategorieliste

import java.sql.Connection
import java.sql.DriverManager


// Klasse für die Erstellung der Produktliste in der Unterkategorie
class SubCategory(val name: String) {

    // Erstellen eine companion object damit die Methode "getSubcategoriesByMainCategory" direkt über den Klassennamen aufgerufen werden kann
    // und keine Instanz der Klasse benötigt (Kapselung)
    companion object {
        fun getSubcategoriesByMainCategory(mainCategoryName: String): List<SubCategory> {
            // Liste erzeugen
            val subCategories = mutableListOf<SubCategory>()
            // Datenbank Verbindung herstellen und Anweisungen
            val connection: Connection = DriverManager.getConnection("jdbc:sqlite:Database.db")
            val query = "SELECT name FROM subCategory WHERE mainCategoryLink = ?"
            val preparedStatement = connection.prepareStatement(query)
            preparedStatement.setString(1, mainCategoryName)

            // Daten aus der Tabelle holen und in Liste hinzufügen
            val resultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                val name = resultSet.getString("name")
                subCategories.add(SubCategory(name))
            }

            resultSet.close()
            preparedStatement.close()
            connection.close()

            return subCategories
        }
    }
}
