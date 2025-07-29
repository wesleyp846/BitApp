package com.wesley.bitapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wesley.bitapp.ui.theme.BitAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitAppTheme {
                BitcoinPriceScreen()
            }
        }
    }
}

@Composable
fun BitcoinPriceScreen() {
    // Etapa 1: Estado básico com texto inicial
    var price by remember { mutableStateOf("Carregando...") }

    // Etapa 2: Texto price vai ser atualizadopela fetchBitcoinPrice()
    LaunchedEffect(Unit) {
        while (true) {
            price = fetchBitcoinPrice() // <<< Atualiza o preço com o resultado da requisição
            delay(30_000) // 30.000 milissegundos = 30 segundos
        }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)){
                Text(
                    text = "Investido R$ 187,99 e $33,56 em 23/07/25",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = price,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

// >>> FUNÇÃO QUE FAZ A REQUISIÇÃO HTTP E PARSEIA O JSON <<<
suspend fun fetchBitcoinPrice(): String = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eth,usd,brl")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val eth = json.getJSONObject("bitcoin").getDouble("eth")
            val usd = json.getJSONObject("bitcoin").getDouble("usd")
            val brl = json.getJSONObject("bitcoin").getDouble("brl")
//            "R$ %.2f".format(brl)
            val formatoBR = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
            val formatoUS = NumberFormat.getCurrencyInstance(Locale.US)
            val formatoETH = NumberFormat.getNumberInstance().apply {
                maximumFractionDigits = 8 // ETH pode ter muitas casas decimais
            }

            val precoBRL = formatoBR.format(brl)
            val precoUSD = formatoUS.format(usd)
            val precoETH = formatoETH.format(eth)

            "Bitcoin:\nBRL: $precoBRL\nUSD: $precoUSD\nETH: $precoETH"
        } else {
            "Erro: Código ${connection.responseCode}"
        }
    } catch (e: Exception) {
        Log.e("BitApp", "Erro na conexão", e)
        "Erro na conexão"
    }
}