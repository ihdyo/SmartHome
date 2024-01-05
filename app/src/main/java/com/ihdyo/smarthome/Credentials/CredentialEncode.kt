import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64

@RequiresApi(Build.VERSION_CODES.O)
fun main() {

    val email = "smarthome.rumahpintar@gmail.com"
    val password = "inikatasandi"

    val concatenate = "$email $password"

    // Encode to Base64
    val encodedBytes = Base64.getEncoder().encode(concatenate.toByteArray())
    val encodedString = String(encodedBytes, Charsets.UTF_8)

    println("Encoded String: $encodedString")

}
