
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.pi_iii_grupo6.InternetMonitor
import com.example.pi_iii_grupo6.NoConnectionActivity

// Classe para lidar com o estado de rede
open class BasicaActivity : AppCompatActivity() {

    private lateinit var networkMonitor: InternetMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkMonitor = InternetMonitor(this)
        networkMonitor.observe(this, Observer { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NoConnectionActivity::class.java)
                startActivity(intent)
            }
        })
    }
}
