// Modul mit 8 Funktion die mit der Datenbank agieren
// addProducts() -> Produkt hinzufügen
// delProduct() -> Produkt löschen
// orderProduct() -> Produktbestand erhöhen
// addAccount() -> Account erstellen
// showAccounts() -> Accountliste
// showProdukts() -> Produktliste
// addToCard() -> Funktion um Produkt zum Warenkorb hinzuzufügen
// showCard() -> Zeigt den aktuellen Warenkorb an


// Importieren vom SQL Klassen/Bibliotheken/Frameworks um bestimmte Funktionen zu erhalten
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException


// Verbindung zur Datenbank herstellen
val url = "jdbc:sqlite:Database.db"
val connection = DriverManager.getConnection(url)
val statement = connection.createStatement()


// Funktion um ein Produkt zum Bestand hinzuzufügen
fun addProducts() {

    // SQL-Statement zum Einfügen eines neuen Produkts
    val insertProducts = "INSERT INTO products (product_id, subCategoryLink, name, price, amount, review) VALUES (?, ?, ?, ?, ?, ?)"
    val preparedStatement: PreparedStatement = connection.prepareStatement(insertProducts)

    // Ein Statement-Objekt erstellen, um Abfragen auszuführen
    //val statement = connection.createStatement()

    println("\nBitte Unterkategorie auswählen")
    println("[1] Gaming-PC,[2] Business-PC, [3] Workstations, [4] Windows-Laptop, [5] Macbook, [6] Drucker, [7] Monitore, [8] Eingabegeräte")

    val categoryChoice = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${addProducts()}")
        validInput
    }

    val choice = when(categoryChoice) {
        1 -> "Gaming-PC"
        2 -> "Business-PC"
        3 -> "Workstations"
        4 -> "Windows-Laptop"
        5 -> "Macbook"
        6 -> "Drucker"
        7 -> "Monitore"
        8 -> "Eingabegeräte"
        else -> {
            println("Keine gültige Eingabe!")
            return  // Bricht die Funktion ab, wenn eine ungültige Auswahl getroffen wird
        }
    }
    // SQL-Abfrage, um die letzte (höchste) `product_id` abzurufen
    val getLastIdQuery = "SELECT product_id FROM products ORDER BY product_id DESC LIMIT 1"
    val resultSet = statement.executeQuery(getLastIdQuery)

    // Die neue `product_id` wird auf die letzte ID + 1 gesetzt; falls keine vorhanden, auf 1
    val newProductId = if (resultSet.next()) resultSet.getInt("product_id") + 1 else 1
    resultSet.close()

    println("Produktbezeichnung eingeben:")
    val productName = readln()
    println("Preis eingeben:")
    val productPrice = readln().toDouble()
    println("Menge eingeben:")
    val productAmount = readln().toInt()

    // Setzen der Werte im PreparedStatement
    preparedStatement.setInt(1, newProductId)
    preparedStatement.setString(2, choice)
    preparedStatement.setString(3, productName)
    preparedStatement.setDouble(4, productPrice)
    preparedStatement.setInt(5, productAmount)
    preparedStatement.setString(6, "") // Hier kannst du eine Standardbewertung setzen, falls erforderlich

    // Ausführen der SQL-Insert-Anweisung, um die Daten in die Datenbank einzufügen
    preparedStatement.executeUpdate()

    // Schließen der Statement-Objekte und Verbindung
    preparedStatement.close()
    statement.close()
    connection.close()
    println("Das Produkt: -$productName- mit dem Preis für $productPrice € und Bestand i.H.v. $productAmount wurde erfolgreich hinzugefügt!")
}


// Funktion um ein Produkt vom Bestand zu löschen
fun delProduct() {
    // Bestandsliste anzeigen
    showProdukts()

    println("### Produkt löschen ###")
    // Hole den aktuellen Bestand aus der Tabelle basierend auf der ID
    println("Bitte die Produkt-ID eingeben um das Produkt aus dem Bestand zu löschen:")

    val userInputID = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()
            ?: throw java.lang.IllegalArgumentException("${errorMessage()} ${delProduct()}")
        validInput
    }

        // Verbindung zur Datenbank herstellen
        val url = "jdbc:sqlite:Database.db"
        val connection = DriverManager.getConnection(url)

        try {
            // SQL-Query zum Löschen der Produktzeile anhand der product_id
            val deleteQuery = "DELETE FROM products WHERE product_id = ?"
            val deleteStatement = connection.prepareStatement(deleteQuery)

            // Setze den Wert für product_id im PreparedStatement
            deleteStatement.setInt(1, userInputID)

            // Führe die DELETE-Anweisung aus
            val rowsDeleted = deleteStatement.executeUpdate()

            if (rowsDeleted > 0) {
                println("Erfolgreiche Löschung!")
            } else {
                println("Produkt nicht gefunden!")
            }

        } catch (e: SQLException) {
            // Fehlerbehandlung, falls ein Fehler auftritt
            println("Eingabe nicht erkannt! Bitte erneut versuchen!")
        } finally {
            // Schließe die Verbindung und das Statement
            connection.close()
        }
}


// Funktion um den Bestand eines Produkts zu erhöhen
fun orderProduct() {
    // Bestandliste anzeigen
    showProdukts()

    println("### Produkt auffüllen")
    // Hole den aktuellen Bestand aus der Tabelle basierend auf der ID

    println("Bitte die Produkt-ID eingeben um Bestand zu erhöhen:")
    val userInputID = readln().toInt()
    println("Bitte die Menge zur Bestandserhöhung eingeben:")
    val userInputAmount = readln().toInt()

    val query = "SELECT amount FROM products WHERE product_id = ?"
    val statement = connection.prepareStatement(query)
    statement.setInt(1, userInputID)
    val resultSet = statement.executeQuery()

    if (resultSet.next()) {
        // Aktuellen Bestand holen
        val currentAmount = resultSet.getInt("amount")

        // Bestand um den gewünschten Wert erhöhen
        val newAmount = currentAmount + userInputAmount

        // Update-Query, um den neuen Bestand in der Tabelle zu speichern
        val updateQuery = "UPDATE products SET amount = ? WHERE product_id = ?"
        val updateStatement = connection.prepareStatement(updateQuery)
        updateStatement.setInt(1, newAmount) // Ändert den amount Wert
        updateStatement.setInt(2, userInputID) // Bedingung für die amount Zeile

        // Ausführen des Updates
        updateStatement.executeUpdate()

        // Bestätigung der Aktualisierung
        println("Der Bestand wurde auf $newAmount erhöht!")

        // Ressourcen schließen
        //updateStatement.close()
    } else {
        println("Produkt mit ID $userInputID nicht gefunden.")
    }
    // Ressourcen schließen
    resultSet.close()
    statement.close()
}


// Funktion zum Hinzufügen eines Accounts
fun addAccount() {
    println("Bitte Namen eingeben")
    val userNameInput = readln()
    println("Bitte Passwort eingeben")
    val userPasswordInput = readln()
    println("Bitte Passwort wiederholen")
    val userRePasswordInput = readln()
    println("Bitte Alter eingeben:")

    val userAgeInput = safeExecute {
        val input = readln()
        val validInput = input?.toIntOrNull()
            ?: throw IllegalArgumentException("${errorMessage()} ${addAccount()}")

        validInput
    } ?: return  // Bei validInput = Null dann soll das Programm beendet werden, ansonst problem mit dem Operator in der
    // folgenden if Anweisung!


    if (userAgeInput < 12) {
        println("\nAchtung! Du bist noch unter 12 Jahre alt! Wir können leider keinen Account für dich erstellen!")
        return
    } else {

        println("Bitte Zahlungsmethode auswählen:\n[1] Paypal\n[2] Überweisung\n[..] Bei Falscheingabe wird kein Zahlungsmittel gespeichert ")
        val userPaymentMethodeInput = readln().toIntOrNull()
        // Umwandlung der Eingabe in ein gültiges Format für die Datenbankspeicherung
        val savePayment = when (userPaymentMethodeInput) {
            1 -> "Paypal"
            2 -> "Überweisung"
            else -> ""
        }
        // Prüft ob die Passworteingaben indentisch sind, ansonsten speichere die Daten in die Datenbank ab
        if (userPasswordInput != userRePasswordInput) {
            println("Die Passwörter stimmen nicht überein! Bitte erneut versuchen")
            addAccount()
        } else {
            val insertSQL =
                "INSERT INTO accounts (userName, userPassword, userAge, paymentMethod) VALUES (?, ?, ?, ?)"
            val preparedStatement: PreparedStatement = connection.prepareStatement(insertSQL)
            preparedStatement.setString(1, userNameInput)
            preparedStatement.setString(2, userPasswordInput)
            preparedStatement.setInt(3, userAgeInput)
            preparedStatement.setString(4, savePayment)

            preparedStatement.executeUpdate()
            preparedStatement.close()

            println("\nAccount wurde erstellt! Herzlich Willkommen $userNameInput")

            // Hauptmenü aufrufen
            mainMenue()
        }
    }
}



// Funktion zum Anzeigen aller vorhandenen Accounts
fun showAccounts() {
    println("\n### Accountliste ###")
    val selectSQL = "SELECT * FROM accounts"
    val resultSet = connection.createStatement().executeQuery(selectSQL)

    while (resultSet.next()) {
        val userName = resultSet.getString("userName")
        val password = resultSet.getString("userPassword")
        val userAge = resultSet.getString("userAge")
        val paymentMethod = resultSet.getString("paymentMethod")
        println("Accountname: $userName, Passwort: $password, Alter: $userAge, Zahlungsmethode: $paymentMethod")
    }
    resultSet.close()
    adminMenue()
}


// Funktion zur Anzeige der Produkt-Bestandsliste
fun showProdukts() {
    // SQL-Abfrage, um die Daten aus den Haupt- und Unterkategorien sowie den Produkten abzurufen
    val query = """
        SELECT mc.name AS hauptkategorie, sc.name AS unterkategorie, p.product_id AS produktid, 
        p.name AS produktname, p.price AS price, p.amount AS amount, p.review AS review 
        FROM products p
        JOIN subCategory sc ON p.subCategoryLink = sc.name
        JOIN mainCategory mc ON sc.mainCategoryLink = mc.name
    """

    val resultSet = statement.executeQuery(query)

    // Die Ergebnismenge durchlaufen und jede Zeile anzeigen
    while (resultSet.next()) {
        val mainCategoryProdukt = resultSet.getString("hauptkategorie")
        val subCategoryProdukt = resultSet.getString("unterkategorie")

        val produktident = resultSet.getString("produktid")
        val productname = resultSet.getString("produktname")
        val pricesing = resultSet.getString("price")
        val inventory = resultSet.getInt("amount")
        val reviews = resultSet.getString("review")

        println("Produkt-ID: $produktident")
        println("### Kategorie ###")
        println("Hauptkategorie: $mainCategoryProdukt")
        println("Unterkategorie: $subCategoryProdukt")
        println("    ### Produktdaten ###")
        println("    Bezeichnung: $productname")
        println("    Preis: $pricesing")
        println("    Bestand: $inventory")
        println("    Rezension: $reviews")

        println("-----------------------------------------")
    }
}

// Funktion um Produkt zum Warenkorb hinzufügen
fun addToCard(product_id: Int) {
    try {
        // Prepare query to select `name` and `price` from `products` where `product_id` matches
        val selectQuery = "SELECT name, price FROM products WHERE product_id = ?"
        val selectStatement = connection?.prepareStatement(selectQuery)
        selectStatement?.setInt(1, product_id)
        val resultSet = selectStatement?.executeQuery()

        // Check if the product exists
        if (resultSet != null && resultSet.next()) {
            // Get name and price from the products table
            val itemName = resultSet.getString("name")
            val itemPrice = resultSet.getDouble("price")

            // Prepare query to insert into shoppingCard table
            val insertQuery = "INSERT INTO shoppingCard (item, price) VALUES (?, ?)"
            val insertStatement = connection.prepareStatement(insertQuery)
            insertStatement.setString(1, itemName)
            insertStatement.setDouble(2, itemPrice)

            // Execute the insert
            insertStatement.executeUpdate()
            println("Produkt '$itemName' zum Warenkorb hinzugefügt.")
            // Close insert statement
            insertStatement.close()
            produktList()

        } else {
            println("Produkt mit ID $product_id nicht gefunden.")
        }

        // Close result set and select statement
        resultSet?.close()
        selectStatement?.close()
    } catch (e: SQLException) {
        println("Datenbankfehler: ${e.message}")
    } finally {
        // Close the connection
        connection?.close()
    }
}


// Funktion um den Warenkorb einzusehen
fun showCard() {
    try {
        // SQL-Abfrage, um alle Daten aus der Tabelle shoppingCard zu holen
        val query = "SELECT * FROM shoppingCard"
        val statement = connection?.createStatement()
        val resultSet = statement?.executeQuery(query)

        var user: String? = null
        var paymentMethod: String? = null
        var totalPrice = 0.0

        println("\n### Warenkorb ###")
        val items = mutableListOf<String>()

        // Durchlaufe alle Zeilen der Tabelle
        var firstLine = true  // Flag, um zu überprüfen, ob es sich um die erste Zeile handelt
        while (resultSet?.next() == true) {
            // Hole den User und die Zahlungsmethode nur einmal
            if (user == null) {
                user = resultSet.getString("user") // Annahme: 'user' ist der Name der Spalte für den Benutzer
                paymentMethod = resultSet.getString("paymentMethod") // Annahme: 'paymentMethod' ist der Name der Spalte
            }

            // Hole die Werte für Artikel und Preis
            val item = resultSet.getString("item")
            val price = resultSet.getDouble("price")

            // Addiere den Preis zum Gesamtpreis
            totalPrice += price

            // Wenn es nicht die erste Zeile ist, füge das Produkt und den Preis zur Liste hinzu
            if (!firstLine) {
                items.add("$item - $price €")
            }

            firstLine = false  // Setze das Flag für die nächste Iteration
        }

        // Ausgabe der User- und Zahlungsmethode (nur einmal)
        if (user != null && paymentMethod != null) {
            println("Hallo $user!")
            println("Deine voreingestellte Zahlungsmethode: $paymentMethod")
        }

        // Ausgabe der Produktliste (beginnend mit der zweiten Zeile)
        if (items.isNotEmpty()) {
            println("\n### Produkte in deinem Warenkorb ###")
            items.forEach { println(it) }
        } else {
            println("Keine Produkte im Warenkorb.")
        }

        // Ausgabe des Gesamtpreises
        println("\nGesamtpreis: %.2f €".format(totalPrice))

        resultSet?.close()
        statement?.close()

        // Zurück zum Kunden-Menü
        customerMenue()

    } catch (e: SQLException) {
        println("Datenbankfehler: ${e.message}")
    } finally {
        connection?.close()
    }
}