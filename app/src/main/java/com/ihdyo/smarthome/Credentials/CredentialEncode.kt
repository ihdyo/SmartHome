import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64

@RequiresApi(Build.VERSION_CODES.O)
fun main() {

    val email = "yodhi.himatika@gmail.com"
    val password = "inikatasandi@123"

    val concatenate = "$email $password"

    // Encode to Base64
    val encodedBytes = Base64.getEncoder().encode(concatenate.toByteArray())
    val encodedString = String(encodedBytes, Charsets.UTF_8)

    println("Encoded String: $encodedString")

}
