package com.example.pizzadapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pizzadapp.ui.theme.PizzaDAppTheme
import java.text.SimpleDateFormat
import java.util.*

// Data classes to represent Pizza and Order
data class Pizza(val name: String, val size: String, val toppings: List<String>, val price: Double, val imageResId: Int? = null)
data class Order(val pizzas: MutableList<Pizza> = mutableListOf(), var customerName: String = "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzaDAppTheme {
                // Setting up the main app structure with a Scaffold
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PizzaOrderApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PizzaOrderApp(modifier: Modifier = Modifier) {
    // State to keep track of the current screen and order
    var currentScreen by remember { mutableStateOf(listOf("FrontPage")) }
    var order by remember { mutableStateOf(Order()) }


    fun navigateTo(screen: String) {
        currentScreen = currentScreen + screen
    }

    fun goBack() {
        if (currentScreen.size > 1) {
            currentScreen = currentScreen.dropLast(1)
        }

    fun toMenu(){
        if (currentScreen.size > 1){
            currentScreen = currentScreen.dropLast(2)
        }
    }


    }

    // Navigation between screens
    when (currentScreen.last()) {
        "FrontPage" -> FrontPage(
            onCustomPizzaClick = { navigateTo("CustomPizza") },
            onPreMadePizzaClick = { navigateTo("PreMadePizza") }
        )
        "CustomPizza" -> CustomPizzaScreen(
            order = order,
            onOrderUpdate = { order = it },
            onCompleteOrder = { navigateTo("Receipt") },
            onGoback =  { goBack() }
        )
        "PreMadePizza" -> PreMadePizzaScreen(
            order = order,
            onOrderUpdate = { order = it },
            onCompleteOrder = { navigateTo("Receipt") },
            onGoback =  { goBack() }
        )
        "Receipt" -> Receipt(
            order = order,
            onGoback = { goBack()}
        )
    }
}

@Composable
fun FrontPage(onCustomPizzaClick: () -> Unit, onPreMadePizzaClick: () -> Unit) {
    // Front page layout with two main options
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Pizza Ordering App", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onCustomPizzaClick) {
            Text("Create Custom Pizza")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onPreMadePizzaClick) {
            Text("Choose Pre-made Pizza")
        }
    }
}

@Composable
fun CustomPizzaScreen(
    order: Order,
    onOrderUpdate: (Order) -> Unit,
    onCompleteOrder: () -> Unit,
    onGoback: () -> Unit
) {
    // State for custom pizza options
    var customerName by remember { mutableStateOf(order.customerName) }
    var selectedSize by remember { mutableStateOf("") }
    var selectedToppings by remember { mutableStateOf(setOf<String>()) }


    Column(modifier = Modifier.padding(16.dp)) {

            // Added Go Back button
            Button(onClick = onGoback) {
                Text("<")
            }

        // Customer name input
        OutlinedTextField(
            value = customerName,
            onValueChange = {
                customerName = it
                onOrderUpdate(order.copy(customerName = it))
            },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pizza size selection
        Text("Pizza Size", fontWeight = FontWeight.Bold)
        Row {
            listOf("Small", "Medium", "Large").forEach { size ->
                RadioButton(
                    selected = selectedSize == size,
                    onClick = { selectedSize = size }
                )
                Text(size)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toppings selection
        Text("Toppings", fontWeight = FontWeight.Bold)
        listOf("Cheese", "Pepperoni", "Mushrooms", "Onions", "Olives", "Sausage").forEach { topping ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = topping in selectedToppings,
                    onCheckedChange = { checked ->
                        selectedToppings = if (checked) {
                            selectedToppings + topping
                        } else {
                            selectedToppings - topping
                        }
                    }
                )
                Text(topping)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add pizza to order button
        Button(
            onClick = {
                if (selectedSize.isNotEmpty()) {
                    val newPizza = Pizza("Custom", selectedSize, selectedToppings.toList(), calculatePrice(selectedSize, selectedToppings))
                    onOrderUpdate(order.copy(pizzas = (order.pizzas + newPizza).toMutableList()))
                    selectedSize = ""
                    selectedToppings = setOf()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Pizza")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display current order
        Text("Current Order:", fontWeight = FontWeight.Bold)
        LazyColumn {
            items(order.pizzas) { pizza ->
                Text("${pizza.size} ${pizza.name} Pizza with ${pizza.toppings.joinToString(", ")}: $${String.format("%.2f", pizza.price)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complete order button
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onCompleteOrder,
            modifier = Modifier.align(Alignment.End),
            enabled = order.pizzas.isNotEmpty() && order.customerName.isNotBlank()
        ) {
            Text("Complete Order")
        }


    }
}

@Composable
fun PreMadePizzaScreen(
    order: Order,
    onOrderUpdate: (Order) -> Unit,
    onCompleteOrder: () -> Unit,
    onGoback: () -> Unit
) {
    var customerName by remember { mutableStateOf(order.customerName) }

    // Pre-defined list of pizzas
    val preMadePizzas = listOf(
        Pizza("Margherita", "Medium", listOf("Cheese", "Tomato"), 9.99, R.drawable.margherita),
        Pizza("Pepperoni", "Medium", listOf("Cheese", "Pepperoni"), 11.99, R.drawable.pepperoni),
        Pizza("Vegetarian", "Medium", listOf("Cheese", "Mushrooms", "Onions", "Olives"), 10.99, R.drawable.vegetarian)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onGoback) {
            Text(text = "<")
            
        }
        // Customer name input
        OutlinedTextField(
            value = customerName,
            onValueChange = {
                customerName = it
                onOrderUpdate(order.copy(customerName = it))
            },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display pre-made pizzas
        Text("Pre-made Pizzas", fontWeight = FontWeight.Bold)
        LazyColumn {
            items(preMadePizzas) { pizza ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        // Pizza image
                        pizza.imageResId?.let { resId ->
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = pizza.name,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        // Pizza details
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(pizza.name, fontWeight = FontWeight.Bold)
                            Text("Toppings: ${pizza.toppings.joinToString(", ")}")
                            Text("Price: $${String.format("%.2f", pizza.price)}")
                            Button(
                                onClick = {
                                    onOrderUpdate(order.copy(pizzas = (order.pizzas + pizza).toMutableList()))
                                }
                            ) {
                                Text("Add to Order")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display current order
        Text("Current Order:", fontWeight = FontWeight.Bold)
        LazyColumn {
            items(order.pizzas) { pizza ->
                Text("${pizza.size} ${pizza.name} Pizza: $${String.format("%.2f", pizza.price)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complete order button
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onCompleteOrder,
            modifier = Modifier.align(Alignment.End),
            enabled = order.pizzas.isNotEmpty() && order.customerName.isNotBlank()
        ) {
            Text("Complete Order")
        }
    }
}

@Composable
fun Receipt(
    order: Order,
    onGoback: () -> Unit
) {



    // Display the final receipt
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Order Receipt", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Customer: ${order.customerName}")
        Text("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        Divider()
        order.pizzas.forEachIndexed { index, pizza ->
            Text("Pizza ${index + 1}: ${pizza.size} ${pizza.name} - $${String.format("%.2f", pizza.price)}")
            Text("  Toppings: ${pizza.toppings.joinToString(", ")}")
        }
        Divider()
        val total = order.pizzas.sumOf { it.price }
        Text("Total: $${String.format("%.2f", total)}", fontWeight = FontWeight.Bold)
        Text("Thank you for your order!")


        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onGoback ,) {
            Text(text = "Go Back to Main Menu")

        }
    }
}

// Function to calculate pizza price based on size and toppings
fun calculatePrice(size: String, toppings: Set<String>): Double {
    val basePrice = when (size) {
        "Small" -> 8.99
        "Medium" -> 10.99
        "Large" -> 12.99
        else -> 0.0
    }
    return basePrice + (toppings.size * 0.5)
}