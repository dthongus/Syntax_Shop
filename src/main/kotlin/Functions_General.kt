// Modul mit 7 Funktionen für Auswahlmenüs
// mainMenue() -> Zeigt das Hauptmenü an
// customerMenue() -> Zeigt das Kundenmenü an
// adminMenue() -> Zeigt das Admin Menü an
// login() -> Zeigt das Login Menü an
// addReviews() -> Funktion um eine Produktbewertung abzugeben
// produktList() ->  Zeigt die Produktliste anhand der Auswahl von Kategorien
// totalProductList() -> Zeigt Produktliste mit allen Produkten an


import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet


fun main(){ mainMenue()}

// Aufruf des Hauptmenü
fun mainMenue() {
    println("\n### Willkommen im PC-Store© Germany ###")
    println("\n### Hauptmenü ###")
    println("[1] Anmelden\n[2] Account erstellen\n[3] Beenden")
    println("\nBitte Auswahl treffen:")


    val userinput = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${customerMenue()}")
        validInput
    }

    // Nutzereingabe wird verarbeitet und an die jeweilige Funktion übergeben
    when (userinput.toInt()) {
        1 -> login()  // Aufruf des Login Menüs
        2 -> addAccount() // Aufruf des Account Menüs zum Anlegen eines neuen Accounts
        3 -> return println("Auf wiedersehen!") // Beendet das Programm
        else -> {  // Falls Eingabe des Nutzers nicht in der Auswahlliste vorhanden ist
            println("Eingabe nicht erkannt! Bitte erneut eingeben:") // Fehlermeldung
            mainMenue()  // Ruft bei Fehleingabe des Nutzers die Funktion (rekursiv) erneut auf.
        }
    }
}


// Aufruf des Kundenmenü
fun customerMenue() {

    println("\n### Mein Kunden Menü ###")
    println("[1] Mein Warenkorb, [2] zum Shop, [3] Alle Produkte, [4] Produkt bewerten")
    println("Bitte Auswahl eingeben:")

    val userInputCustom = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${customerMenue()}")
        validInput
    }

    when (userInputCustom) {
        1 -> showCard()
        2 -> produktList()
        3 -> totalProductList()
        4 -> addReviews()
        else -> {
            println("Eingabe nicht erkannt! Bitte erneut eingeben.")
            customerMenue() // Funktion rekursiv erneut aufrufen
        }
    }

    // Ressourcen schließen
    statement.close()
    connection.close()
}


// Aufruf des Adminmenü
fun adminMenue() {
    println("\n### Admin Menü ###")
    println("[1] Produkt hinzufügen\n[2] Produkt entfernen\n[3] Produktbestand auffüllen\n" +
            "[4] Accountliste\n[5] Beenden")
    println("Bitte Auswahl treffen:")

    val adminInput = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${adminMenue()}")
        validInput

    }

    when (adminInput) {
        1 -> addProducts()
        2 -> delProduct()
        3 -> orderProduct()
        4 -> showAccounts()
        5 -> return
        else -> println("Eingabe nicht erkannt! Bitte erneut versuchen: ${adminMenue()}")
    }
}


// Aufruf des Login Menü
fun login() {
    println("\n### Anmeldung ###")
    println("\nGeben Sie Ihren Benutzernamen ein:")
    val username = readln()
    println("Geben Sie Ihr Passwort ein:")
    val password = readln()

    // Verbindung zur Datenbank aufbauen
    val url = "jdbc:sqlite:Database.db"
    val connection = DriverManager.getConnection(url)

    // Warenkorb Daten leeren
    val deleteQuery = "DELETE FROM shoppingCard"
    val deleteStatement = connection.createStatement()
    deleteStatement.executeUpdate(deleteQuery)

    // Prüfen ob die Eingaben in der der Datenbank vorhanden sind
    val query = "SELECT * FROM accounts WHERE userName = ? AND userPassword = ?"
    val statement: PreparedStatement = connection.prepareStatement(query)
    statement.setString(1, username)
    statement.setString(2, password)

    // Ergebnis speichern
    val resultSet: ResultSet = statement.executeQuery()

    // IF Abfrage ob Daten vorhanden sind oder nicht
    if (resultSet.next()) {
        println("\nAnmeldung erfolgreich! Herzlich Willkommen $username")

        // Prüfen ob ein Kunde oder Admin sich eingeloggt hat
        if (username == "Admin") {
            // Falls sich ein Admin angemeldet hat
            deleteStatement.close()
            connection.close()
            adminMenue()

        } else {  // Falls sich ein Kunde angemeldet hat
            // Einfügen der Nutzer Daten in seine shoppingCard (Warenkorb)
            val insertProducts = "INSERT INTO shoppingCard (user, paymentMethod) VALUES (?, ?)"
            val preparedStatement: PreparedStatement = connection.prepareStatement(insertProducts)

            // Zahlungsmethode anhand des Nutzernamens auslesen und in Warenkorb einfügen
            val paymentMethodQuery = "SELECT paymentMethod FROM accounts WHERE userName = ?"
            val paymentMethodStatement: PreparedStatement = connection.prepareStatement(paymentMethodQuery)
            paymentMethodStatement.setString(1, username)

            // Ausführen der Abfrage und Abrufen des paymentMethod-Werts
            val paymentMethodResult: ResultSet = paymentMethodStatement.executeQuery()
            if (paymentMethodResult.next()) {
                val paymentMethod = paymentMethodResult.getString("paymentMethod")

                // Setzen der Werte im PreparedStatement
                preparedStatement.setString(1, username)
                preparedStatement.setString(2, paymentMethod)

                // Ausführen der SQL-Insert-Anweisung, um die Daten in die Datenbank einzufügen
                preparedStatement.executeUpdate()

                // Schließen der Statement-Objekte und Verbindung
                preparedStatement.close()
                paymentMethodStatement.close()
                deleteStatement.close()
                connection.close()

                customerMenue()
            }
        }
    } else {
        println("\nBenutzername oder Passwort ist falsch! Bitte erneut versuchen!")
        login()
    }
}


// Funktion um Produktbewertung abzugeben
fun addReviews() {
    // Aufruf der Funktion zur Anzeige der Produktliste
    showProdukts()

    val reviewManager = productReviewManager()

    // Abfrage welches Produkt bewertet werden soll
    println("\nBitte die Produkt-ID eingeben für das zu bewertende Produkt:")
    val productId = readln().toInt()
    // Bewertung eingeben
    println("Geben Sie die Bewertung ein:")
    val review = readln()

    // Aufruf der Klasse zur Speicherung der Bewertung
    reviewManager.submitReview(productId, review)
}


// Aufruf des der Produktliste
fun produktList() {
    println("\n### Hauptkategorie ###")
    val mainCategories = MainCategory.getAllCategories()
    mainCategories.forEachIndexed { index, category -> println("${index + 1}. ${category.name}") }

    // Let the user choose a main category
    print("\nWählen Sie anhand der Nummer eine Kategorie aus oder\nWarenkorb anzeigen die [4] ein:")
    val userInputMainCategory = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()?.minus(1)
            ?: throw IllegalArgumentException("${errorMessage()} ${produktList()}")
        validInput
    }

    if (userInputMainCategory == 3) {
        showCard()
    } else {
        userInputMainCategory?.let { mainIndex ->
            if (mainIndex in mainCategories.indices) {
                val mainCategoryName = mainCategories[mainIndex].name

                println("\n### Unterkategorie: $mainCategoryName ###")
                val subCategories = SubCategory.getSubcategoriesByMainCategory(mainCategoryName)
                subCategories.forEachIndexed { index, subCategory -> println("${index + 1}. ${subCategory.name}") }

                // Let the user choose a subcategory
                print("\nWählen Sie eine Unterkategorie aus: ")
                val userInputSubCategory = readLine()?.toIntOrNull()?.minus(1)

                userInputSubCategory?.let { subIndex ->
                    if (subIndex in subCategories.indices) {
                        val subCategoryName = subCategories[subIndex].name

                        println("\n### Produkte ###")
                        val products = Productlist.getProductsBySubCategory(subCategoryName)

                        products.forEach { product ->
                            println("Produkt-ID: ${product.productId} | Artikel: ${product.name} | Preis: ${product.price} € | Kundenbewertung: ${product.review}")
                        }

                        println("\nBitte geben Sie die Produkt-ID ein um den Artikel in den Warenkorb hinzuzufügen.")
                        println("Andernfalls geben Sie 0 ein um wieder zur Hauptkategorie zu gelangen:")
                        val userInputProduct = readln().toInt()
                        if (userInputProduct == 0) {
                            produktList()
                        } else {
                            addToCard(userInputProduct)
                        }

                    } else {
                        println("\nNetter Versuch, aber zu Nr. $userInputSubCategory gibt es keine Unterkategorie :)")
                    }
                }
            } else {
                println("\nNetter Versuch, aber zu Nr. $userInputMainCategory gibt es keine Kategorie :)")
            }
        }
    }

}




// Anzeigen der kompletten Produkte als Liste mit Sortierungsfunktionen
fun totalProductList() {
    val productManager = showProductList()

    // Alle Produkte abrufen
    val allProducts = productManager.getAllProducts()

    println("\n### Produktliste ###")
    // Ausgabe der Produktliste
    productManager.printProductList(allProducts)

    // Wiederholung der Menüfunktion, solange bis der Nutzer wieder ins Kundenmenü möchte
    while (true) {
        // Weitere Menü Funktionen
        println("\n### Menü ###")


        println("[1] Sortieren nach Produktbezeichnung, [2] Sortieren nach Preis, [3] Filteroptionen, [4] Zurück zum Menü")
        // Hier Fehlerbehandlung!
        val userInputSorts = safeExecute {
            val input = readln()
            val validInput = input?.toIntOrNull()
                ?: throw IllegalArgumentException("${errorMessage()} ${totalProductList()}")
            validInput

        }

        // Funktionsaufrufe des Nutzers
        when (userInputSorts) {
            1 -> {  // Sortieren der Produktliste nach Produktbezeichnung alphabetisch
                println("\n### Produkte nach Name sortiert ###")
                val productsSortedByName = productManager.sortName(allProducts)
                productManager.printProductList(productsSortedByName)

            }

            2 -> {  // Sortieren der Produktliste nach Preis aufsteigend
                println("\n### Produkte nach Preis sortiert ###")
                val productsSortedByPrice = productManager.sortPrice(allProducts)
                productManager.printProductList(productsSortedByPrice)
            }

            3 -> { // Aufruf der Filterfunktion
                filterOptions(allProducts)
            }

            4 -> {
                customerMenue() // Zurück zum Hauptmenü
                break // Unterbricht die While Schleife
            }
            else -> println("\nEingabe nicht erkannt!")  // Wenn Nutzer keine gültige Eingabe getätigt hat
        }
    }
}


// Filteroptionen anzeigen
fun filterOptions(products: List<Product>) {
    while (true) {
        println("\n### Filteroptionen ###")

        while (true) {
            println("\n### Filteroptionen ###")
            println("[1] Nach Produktbezeichnung filtern, [2] Nach Preis filtern, [3] Zurück")

            // Benutzereingabe für Filteroptionen (mit safeExecute)
            val userInputFilter = safeExecute {
                val input = readLine()?.toIntOrNull()
                    ?: throw IllegalArgumentException("${errorMessage()} ${filterOptions(products)}")
                input
            }

            when (userInputFilter) {
                1 -> {  // Filtern nach Name
                    println("\n### Filter nach Produktbezeichnung ###")
                    println("Produktbezeichnung eingeben:")
                    val name = readln()
                    val filteredByName = showProductList().filterProductname(products, name)
                    if (filteredByName.isEmpty()) {
                        println("Keine Produkte mit dem Namen '$name' vorhanden.")
                    } else {
                        println("\n### Produkte für: '$name' ###")
                        showProductList().printProductList(filteredByName)
                    }
                }

                2 -> {  // Filtern nach Preisbereich
                    println("\n### Filter nach Preis ###")

                    // Eingabe des minimalen Preises, Fehlerbehandlung mit safeExecute
                    val minPrice = safeExecute {
                        println("Preis minimum:")
                        val input = readLine()
                        val validPrice = input?.toDoubleOrNull()
                            //Falls ein Fehlerhafter Wert eingegeben wurde
                            ?: throw IllegalArgumentException("${errorMessage()} ${filterOptions(products)}")
                        // Gibt den Preis zurück, wenn Eingabe korrekt ist
                        validPrice
                    }
                    // Eingabe des minimalen Preises, Fehlerbehandlung mit safeExecute
                    val maxPrice = safeExecute {
                        println("Preis minimum:")
                        val input = readLine()
                        val validPrice = input?.toDoubleOrNull()
                            ?: throw IllegalArgumentException("${errorMessage()} ${filterOptions(products)}")
                        validPrice
                    }
                    // Filter nach Preisbereich anwenden und Ergebnis in einer Liste speichern
                    val filteredProducts = showProductList().filterPrice(products, minPrice, maxPrice)

                    // Ausgabe der gefilterten Produkte
                    if (filteredProducts.isEmpty()) {
                        println("Keine Produkte im Bereich von $minPrice € bis $maxPrice € gefunden.")
                    } else {
                        println("\n### Produkte im Bereich von $minPrice € bis $maxPrice € ###")
                        showProductList().printProductList(filteredProducts)
                    }
                }

                3 -> {
                    break // Zurück zum Menü
                }

                else -> println("Eingabe nicht erkannt!")
            }
        }
    }
}
























