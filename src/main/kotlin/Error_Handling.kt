//// Modul für allgemeine Fehlerbehandlung durch try / catch
fun <T> safeExecute(action: () -> T): T {
    // Bei gültiger Nutzereingabe:
    return try {
        // Aktion auszuführen
        action()
    } catch (e: Exception) {
        // Fehlermeldung
        println("Fehler: ${e.message}")
        // Stoppt das Programm
        System.exit(1)
        // hat keine Funktion, dient lediglich als Platzhalter um einen Rückgabewert an T zu senden.
        throw e
    }
}

// Fehlernachricht
fun errorMessage() {
    println(" ****** Fehler: Ungültige Eingabe! ******")
}


