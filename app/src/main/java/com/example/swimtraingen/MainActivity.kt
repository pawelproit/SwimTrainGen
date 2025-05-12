package com.example.swimtraingen



import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import android.util.Log
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import com.example.swimtraingen.ui.theme.DeepBlack
import com.example.swimtraingen.ui.theme.TextWhite
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import com.example.swimtraingen.ui.theme.AccentBlue
import com.example.swimtraingen.ui.theme.NavyBlue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.swimtraingen.ui.theme.*
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment // Dodaj ten import
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.tasks.await
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException
import android.widget.Toast





class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            UserFlowApp(
                onGoogleLoginClick = { signInWithGoogle() }
            )
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Zalogowano: ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    // przejście do kolejnego ekranu np. AgeScreen
                    setContent {
                        UserFlowApp(
                            onGoogleLoginClick = { signInWithGoogle() },
                            startDestination = "age"
                        )
                    }
                } else {
                    Toast.makeText(this, "Logowanie nieudane", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun UserFlowApp(
    onGoogleLoginClick: () -> Unit = {},
    startDestination: String = "welcome"
) {
    val navController = rememberNavController()
    val viewModel: UserInputViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable("welcome") { WelcomeScreen(navController, onGoogleLoginClick) }
        composable("age") { AgeScreen(navController, viewModel) }
        composable("gender") { GenderScreen(navController, viewModel) }
        composable("weight") { WeightScreen(navController, viewModel) }
        composable("height") { HeightScreen(navController, viewModel) }
        composable("fitness") { FitnessLevelScreen(navController, viewModel) }
        composable("goal") { GoalScreen(navController, viewModel) }
        composable("days") { TrainingDaysScreen(navController, viewModel) }
        composable("main") { MainScreen(viewModel) }
        composable("debug") { DebugAllCollectionsScreen() }
    }
}


class UserInputViewModel : ViewModel() {
    var age by mutableStateOf(0)
    var gender by mutableStateOf("")
    var weight by mutableStateOf(0)
    var height by mutableStateOf(0)
    var fitnessLevel by mutableStateOf(1)
    var goal by mutableStateOf("")
    var trainingDays by mutableStateOf(1)


}


@Composable
fun WelcomeScreen(navController: NavController, onGoogleLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Witaj w SwimPal!",
            color = TextWhite,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Kliknij dalej, aby uzupełnić dane i wygenerować swój pływacki plan treningowy.",
            color = TextWhite,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            onClick = onGoogleLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = TextWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zaloguj się przez Google")
        }

        Button(
            onClick = { navController.navigate("debug") },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,
                contentColor = TextWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Debug Firestore")
        }
    }
}


@Composable
fun AgeScreen(navController: NavController, viewModel: UserInputViewModel) {
    var age by remember { mutableStateOf(viewModel.age) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)  // Czarne tło
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Podaj swój wiek (0–100)",
            color = TextWhite,  // Biały tekst
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = age.toFloat(),
            onValueChange = { age = it.toInt() },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = AccentBlue,  // Niebieski suwak
                activeTrackColor = NavyBlue,  // Granatowa aktywna ścieżka
                inactiveTrackColor = NavyBlue.copy(alpha = 0.3f)
            )
        )

        Text(
            "Wiek: $age lat",
            color = TextWhite  // Biały tekst
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.age = age
                navController.navigate("gender")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,  // Granatowy przycisk
                contentColor = TextWhite  // Biały tekst
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dalej")
        }
    }
}


@Composable
fun GenderScreen(navController: NavController, viewModel: UserInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Wybierz swoją płeć:",
            color = TextWhite,
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    viewModel.gender = "kobieta"
                    navController.navigate("weight")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue,
                    contentColor = TextWhite
                )
            ) {
                Text("Kobieta")
            }
            Button(
                onClick = {
                    viewModel.gender = "mężczyzna"
                    navController.navigate("weight")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue,
                    contentColor = TextWhite
                )
            ) {
                Text("Mężczyzna")
            }
        }
    }
}




@Composable
fun WeightScreen(navController: NavController, viewModel: UserInputViewModel) {
    var weight by remember { mutableStateOf(viewModel.weight) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Podaj swoją wagę (0–200 kg)",
            color = TextWhite,
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = weight.toFloat(),
            onValueChange = { weight = it.toInt() },
            valueRange = 0f..200f,
            colors = SliderDefaults.colors(
                thumbColor = SliderThumb,
                activeTrackColor = NavyBlue,
                inactiveTrackColor = NavyBlue.copy(alpha = 0.3f)
            )
        )
        Text(
            "Waga: $weight kg",
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.weight = weight
                navController.navigate("height")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,
                contentColor = TextWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dalej")
        }
    }
}


@Composable
fun FitnessLevelScreen(navController: NavController, viewModel: UserInputViewModel) {
    var level by remember { mutableStateOf(viewModel.fitnessLevel) }
    val descriptions = listOf(
        "1 – Początkujący",
        "2 – Średniozaawansowany",
        "3 – Zaawansowany"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Wybierz poziom wytrenowania:",
            color = TextWhite,
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = level.toFloat(),
            onValueChange = { level = it.toInt().coerceIn(1, 3) },
            valueRange = 1f..3f,
            steps = 1,
            colors = SliderDefaults.colors(
                thumbColor = SliderThumb,
                activeTrackColor = NavyBlue,
                inactiveTrackColor = NavyBlue.copy(alpha = 0.3f)
            )
        )
        Text(
            descriptions[level - 1],
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.fitnessLevel = level
                navController.navigate("goal")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,
                contentColor = TextWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dalej")
        }
    }
}




@Composable
fun HeightScreen(navController: NavController, viewModel: UserInputViewModel) {
    var height by remember { mutableStateOf(viewModel.height) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Podaj swój wzrost (0–250 cm)",
            color = TextWhite,
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = height.toFloat(),
            onValueChange = { height = it.toInt() },
            valueRange = 0f..250f,
            colors = SliderDefaults.colors(
                thumbColor = SliderThumb,
                activeTrackColor = NavyBlue,
                inactiveTrackColor = NavyBlue.copy(alpha = 0.3f)
            )
        )
        Text(
            "Wzrost: $height cm",
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.height = height
                navController.navigate("fitness")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,
                contentColor = TextWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dalej")
        }
    }
}


@Composable
fun GoalScreen(navController: NavController, viewModel: UserInputViewModel) {
    val goals = listOf("Sprinty", "Triathlon", "Open water", "Technika")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Wybierz swój cel treningowy:",
            color = TextWhite,
            style = MaterialTheme.typography.titleMedium
        )
        goals.forEach { goal ->
            Button(
                onClick = {
                    viewModel.goal = goal
                    navController.navigate("days")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue,
                    contentColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(goal)
            }
        }
    }
}


fun getCollectionName(goal: String, level: Int): String {
    val base = when (goal.lowercase()) {
        "sprinty" -> "treningi_sprinty"
        "triathlon" -> "treningi_triathlon"
        "open water" -> "treningi_open_water"
        "technika" -> "treningi_technika"
        else -> "treningi_default"
    }

    return when (level) {
        1 -> "${base}_0"   // poziom 1
        2 -> base          // poziom 2
        3 -> "${base}_1"   // poziom 3
        else -> base
    }
}



@Composable
fun TrainingDaysScreen(navController: NavController, viewModel: UserInputViewModel) {
    var days by remember { mutableStateOf(viewModel.trainingDays) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ile dni w tygodniu możesz trenować? (1–5)",
            color = TextWhite,
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = days.toFloat(),
            onValueChange = { days = it.toInt().coerceIn(1, 5) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = SliderThumb,
                activeTrackColor = NavyBlue,
                inactiveTrackColor = NavyBlue.copy(alpha = 0.3f)
            )
        )
        Text(
            "Treningów w tygodniu: $days",
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.trainingDays = days
                navController.navigate("main")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyBlue,
                contentColor = TextWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zakończ")
        }
    }
}


data class Training(
    val day: Int,
    val steps: List<Step>
)

data class Step(
    val name: String,
    val description: String,
    val order: Int = 0
)

@Composable
fun MainScreen(viewModel: UserInputViewModel) {
    val db = Firebase.firestore
    var trainings by remember { mutableStateOf(emptyList<Training>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val collectionName = getCollectionName(viewModel.goal, viewModel.fitnessLevel)

    LaunchedEffect(collectionName, viewModel.trainingDays) {
        Log.d("DEBUG", "Pobieram kolekcję: $collectionName")
        try {
            val result = db.collection(collectionName)
                .get()
                .await()

            Log.d("DEBUG", "Pobrano ${result.size()} dokumentów")

            trainings = result.documents
                .shuffled()
                .take(viewModel.trainingDays.coerceIn(1, 6))
                .mapIndexedNotNull { index, doc ->
                    try {
                        Log.d("DEBUG", "Przetwarzam dokument ID: ${doc.id}")
                        val steps = (doc["steps"] as? List<Map<String, Any>>)?.mapNotNull { stepMap ->
                            Log.d("DEBUG", "Krok: ${stepMap["nazwa"]} | ${stepMap["order"]}")
                            Step(
                                name = stepMap["nazwa"]?.toString() ?: "Brak nazwy",
                                description = stepMap["opis"]?.toString() ?: "Brak opisu",
                                order = (stepMap["order"] as? Long)?.toInt() ?: 0
                            )
                        }?.sortedBy { it.order } ?: emptyList()

                        Training(
                            day = index + 1,
                            steps = steps
                        )
                    } catch (e: Exception) {
                        Log.e("DEBUG", "Błąd w dokumencie ${doc.id}: ${e.localizedMessage}", e)
                        null
                    }
                }

            error = if (trainings.isEmpty()) "Brak dostępnych treningów" else null
            Log.d("DEBUG", "Zakończono przetwarzanie treningów: ${trainings.size}")
        } catch (e: Exception) {
            error = "Błąd połączenia: ${e.localizedMessage}"
            Log.e("DEBUG", "Błąd pobierania danych z Firestore: ${e.localizedMessage}", e)
            trainings = emptyList()
        } finally {
            isLoading = false
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Twoje ustawienia:",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    DetailItem("Cel treningowy:", viewModel.goal)
                    DetailItem("Poziom wytrenowania:", mapFitnessLevel(viewModel.fitnessLevel))
                    DetailItem("Liczba dni treningowych:", viewModel.trainingDays.toString())
                }
            }

            when {
                isLoading -> LoadingIndicator()
                !error.isNullOrEmpty() -> ErrorMessage(error!!)
                trainings.isEmpty() -> EmptyState()
                else -> TrainingList(trainings)
            }
        }
    }
}


@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                "Ładowanie planu...",
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = Color.Red,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun EmptyState() {
    Text(
        text = "Brak treningów dla wybranych kryteriów",
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun TrainingList(trainings: List<Training>) {
    var expandedDay by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(trainings) { training ->
            TrainingCard(
                day = training.day,
                steps = training.steps.sortedBy { it.order },
                isExpanded = expandedDay == training.day,
                onCardClick = {
                    expandedDay = if (expandedDay == training.day) null else training.day
                }
            )
        }
    }
}


// Reszta istniejącego kodu (DetailItem, mapFitnessLevel, TrainingCard) pozostaje bez zmian

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

private fun mapFitnessLevel(level: Int): String {
    return when (level) {
        1 -> "Początkujący"
        2 -> "Średniozaawansowany"
        3 -> "Zaawansowany"
        else -> "Nieokreślony"
    }
}

@Composable
fun DebugAllCollectionsScreen() {
    val db = Firebase.firestore
    val collectionNames = listOf(
        "treningi_open_water",
        "treningi_open_water_0",
        "treningi_open_water_1",
        "treningi_sprinty",
        "treningi_sprinty_0",
        "treningi_sprinty_1",
        "treningi_technika",
        "treningi_technika_0",
        "treningi_technika_1",
        "treningi_triathlon",
        "treningi_triathlon_0",
        "treningi_triathlon_1"
    )

    var isLoading by remember { mutableStateOf(true) }
    var logOutput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val builder = StringBuilder()

        for (collection in collectionNames) {
            try {
                val result = db.collection(collection).get().await()
                builder.append("✅ Kolekcja: $collection — ${result.size()} dokumentów\n")

                result.documents.forEach { doc ->
                    builder.append("• Dokument ID: ${doc.id}\n")
                    val steps = doc["steps"] as? List<Map<String, Any>>
                    steps?.forEach { step ->
                        builder.append("   → ${step["nazwa"]} | ${step["order"]} \n")
                    }
                }
            } catch (e: Exception) {
                builder.append("❌ Błąd przy kolekcji $collection: ${e.localizedMessage}\n")
            }
            builder.append("\n")
        }

        logOutput = builder.toString()
        Log.d("DEBUG_FIREBASE", logOutput)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Test pobierania kolekcji z Firestore:",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Text(
                text = logOutput,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}


@Composable
fun TrainingCard(day: Int, steps: List<Step>, isExpanded: Boolean, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Dzień $day",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )

            // Podstawowe kroki treningu
            steps.sortedBy { it.order }.forEach { step ->
                Column {
                    Text(
                        step.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Black
                    )
                    Text(
                        step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Rozsuwana dodatkowa informacja
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Dodatkowe informacje o treningu:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    // Tutaj dodaj tekst, zdjęcia, filmy - przykładowo tekst:
                    Text(
                        "Tutaj możesz umieścić szczegółowe opisy, wskazówki, linki do filmów lub zdjęcia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    // Przykład dodania obrazu lub video wymaga dodatkowych bibliotek i implementacji
                }
            }
        }
    }
}
