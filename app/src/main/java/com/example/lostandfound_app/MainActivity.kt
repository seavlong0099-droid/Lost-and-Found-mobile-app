package com.example.lostandfound_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppBlue = androidx.compose.ui.graphics.Color(0xFF2457D6)
private val LightBlue = androidx.compose.ui.graphics.Color(0xFFEDF3FF)
private val Muted = androidx.compose.ui.graphics.Color(0xFF667085)

enum class AppScreen {
    HOME, SEARCH, REPORT, DETAILS, PROFILE
}

data class FoundItem(
    val id: Int,
    val name: String,
    val category: String,
    val location: String,
    val date: String,
    val description: String,
    val type: String,
    val emoji: String,
    val contact: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = AppBlue,
                    background = androidx.compose.ui.graphics.Color(0xFFF7F8FC),
                    surface = androidx.compose.ui.graphics.Color.White
                )
            ) {
                LostFoundApp()
            }
        }
    }
}

@Composable
fun LostFoundApp() {
    val reports = remember {
        mutableStateListOf(
            FoundItem(
                1, "Black Wallet", "Personal Items",
                "University Library", "Oct 15, 2026",
                "A black wallet found near the library study area.",
                "Found", "👛", "Finder"
            ),
            FoundItem(
                2, "Student ID Card", "Documents",
                "Building A", "Oct 14, 2026",
                "A student ID card was found outside the classroom.",
                "Found", "🪪", "Finder"
            ),
            FoundItem(
                3, "Wireless Earbuds", "Electronics",
                "Campus Cafeteria", "Oct 13, 2026",
                "White wireless earbuds in a small charging case.",
                "Lost", "🎧", "Owner"
            ),
            FoundItem(
                4, "Blue Backpack", "Bags",
                "Parking Area", "Oct 12, 2026",
                "A blue backpack with books inside.",
                "Lost", "🎒", "Owner"
            ),
            FoundItem(
                5, "House Keys", "Personal Items",
                "Main Entrance", "Oct 11, 2026",
                "A set of keys attached to a small keychain.",
                "Found", "🔑", "Finder"
            )
        )
    }

    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedItem by remember { mutableStateOf<FoundItem?>(null) }
    var searchText by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    var nextId by remember { mutableIntStateOf(6) }
    var notice by remember { mutableStateOf("") }

    // Report form state
    var reportType by remember { mutableStateOf("Lost") }
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal Items") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val filteredReports = reports.filter { item ->
        val matchesSearch =
            item.name.contains(searchText, ignoreCase = true) ||
                    item.category.contains(searchText, ignoreCase = true) ||
                    item.location.contains(searchText, ignoreCase = true)

        val matchesType =
            activeFilter == "All" || item.type == activeFilter

        matchesSearch && matchesType
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color(0xFFF7F8FC),
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color.White
            ) {
                val destinations = listOf(
                    Triple(AppScreen.HOME, "Home", "⌂"),
                    Triple(AppScreen.SEARCH, "Search", "⌕"),
                    Triple(AppScreen.REPORT, "Report", "+"),
                    Triple(AppScreen.PROFILE, "Profile", "○")
                )

                destinations.forEach { (destination, label, symbol) ->
                    NavigationBarItem(
                        selected = screen == destination,
                        onClick = {
                            screen = destination
                            notice = ""
                        },
                        icon = {
                            Text(symbol, fontSize = 23.sp)
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (screen) {
                AppScreen.HOME -> HomeScreen(
                    reports = filteredReports,
                    searchText = searchText,
                    onSearchChange = {
                        searchText = it
                        activeFilter = "All"
                    },
                    activeFilter = activeFilter,
                    onFilterChange = { activeFilter = it },
                    onSearchClick = { screen = AppScreen.SEARCH },
                    onReportClick = { screen = AppScreen.REPORT },
                    onItemClick = {
                        selectedItem = it
                        screen = AppScreen.DETAILS
                    }
                )

                AppScreen.SEARCH -> SearchScreen(
                    reports = filteredReports,
                    searchText = searchText,
                    onSearchChange = { searchText = it },
                    activeFilter = activeFilter,
                    onFilterChange = { activeFilter = it },
                    onItemClick = {
                        selectedItem = it
                        screen = AppScreen.DETAILS
                    }
                )

                AppScreen.REPORT -> ReportScreen(
                    reportType = reportType,
                    onTypeChange = { reportType = it },
                    itemName = itemName,
                    onNameChange = { itemName = it },
                    category = category,
                    onCategoryChange = { category = it },
                    location = location,
                    onLocationChange = { location = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    notice = notice,
                    onSubmit = {
                        if (itemName.isBlank() ||
                            location.isBlank() ||
                            description.isBlank()
                        ) {
                            notice = "Please complete all fields."
                        } else {
                            val emoji = when (category) {
                                "Electronics" -> "🎧"
                                "Documents" -> "🪪"
                                "Bags" -> "🎒"
                                else -> "📦"
                            }

                            reports.add(
                                0,
                                FoundItem(
                                    id = nextId++,
                                    name = itemName.trim(),
                                    category = category,
                                    location = location.trim(),
                                    date = "Today",
                                    description = description.trim(),
                                    type = reportType,
                                    emoji = emoji,
                                    contact = "You"
                                )
                            )

                            itemName = ""
                            location = ""
                            description = ""
                            reportType = "Lost"
                            activeFilter = "All"
                            searchText = ""
                            notice = "Report published successfully!"
                            screen = AppScreen.HOME
                        }
                    }
                )

                AppScreen.DETAILS -> {
                    val item = selectedItem
                    if (item != null) {
                        ItemDetailsScreen(
                            item = item,
                            onBack = { screen = AppScreen.HOME },
                            onContact = {
                                notice = "Contact request noted in this demo."
                            },
                            onReturned = {
                                val index = reports.indexOfFirst {
                                    it.id == item.id
                                }
                                if (index >= 0) {
                                    reports[index] = reports[index].copy(
                                        type = "Returned"
                                    )
                                }
                                selectedItem = item.copy(type = "Returned")
                                notice = "Item marked as returned."
                            },
                            notice = notice
                        )
                    }
                }

                AppScreen.PROFILE -> ProfileScreen(
                    myReports = reports.filter {
                        it.contact == "You"
                    },
                    onItemClick = {
                        selectedItem = it
                        screen = AppScreen.DETAILS
                    }
                )
            }
        }
    }
}

@Composable
fun ScreenTitle(
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 12.dp
        )
    ) {
        Text(
            title,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color(0xFF182230)
        )
        if (subtitle != null) {
            Spacer(Modifier.height(5.dp))
            Text(subtitle, color = Muted, fontSize = 14.sp)
        }
    }
}

@Composable
fun HomeScreen(
    reports: List<FoundItem>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    activeFilter: String,
    onFilterChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onReportClick: () -> Unit,
    onItemClick: (FoundItem) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle(
            "Lost & Found",
            "Find what matters. Return what you find."
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppBlue
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Lost something?",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "Search campus reports or let others know what you found.",
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onReportClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.White,
                            contentColor = AppBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("＋  Report an Item")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick() },
                placeholder = { Text("Search items or locations") },
                leadingIcon = { Text("⌕", fontSize = 24.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(20.dp))
            Text(
                "Browse reports",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            FilterRow(activeFilter, onFilterChange)

            Spacer(Modifier.height(12.dp))

            if (reports.isEmpty()) {
                EmptyState("No items found", "Try another search or filter.")
            } else {
                reports.forEach { item ->
                    ItemCard(item, onClick = { onItemClick(item) })
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun SearchScreen(
    reports: List<FoundItem>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    activeFilter: String,
    onFilterChange: (String) -> Unit,
    onItemClick: (FoundItem) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Search Items", "Find a lost or found belonging")

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Name, category, or location") },
            leadingIcon = { Text("⌕", fontSize = 24.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(12.dp))
        FilterRow(activeFilter, onFilterChange)

        if (reports.isEmpty()) {
            EmptyState("No matching items", "Try a different search.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports, key = { it.id }) { item ->
                    ItemCard(item) { onItemClick(item) }
                }
            }
        }
    }
}

@Composable
fun FilterRow(
    activeFilter: String,
    onFilterChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("All", "Lost", "Found").forEach { filter ->
            FilterChip(
                selected = activeFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter) }
            )
        }
    }
}

@Composable
fun ItemCard(
    item: FoundItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .background(LightBlue, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 31.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("📍 ${item.location}", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(item.date, color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))

                val badgeColor = when (item.type) {
                    "Lost" -> androidx.compose.ui.graphics.Color(0xFFFFF0E6)
                    "Returned" -> androidx.compose.ui.graphics.Color(0xFFE3F8E8)
                    else -> LightBlue
                }

                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        item.type,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 4.dp
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text("›", fontSize = 27.sp, color = Muted)
        }
    }
}

@Composable
fun ReportScreen(
    reportType: String,
    onTypeChange: (String) -> Unit,
    itemName: String,
    onNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    notice: String,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenTitle("Report an Item", "Help reconnect belongings with their owners")

        Text("What are you reporting?", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Lost", "Found").forEach { type ->
                FilterChip(
                    selected = reportType == type,
                    onClick = { onTypeChange(type) },
                    label = { Text("$type Item") }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        FormLabel("Item name")
        OutlinedTextField(
            value = itemName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Black wallet") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(14.dp))
        FormLabel("Category")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "Personal Items", "Electronics", "Documents", "Bags"
            ).chunked(2).forEach { rowCategories ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowCategories.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { onCategoryChange(option) },
                            label = { Text(option) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        FormLabel("Location")
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Where was it lost or found?") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(14.dp))
        FormLabel("Description")
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Describe the item to help identify it")
            },
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(20.dp))

        if (notice.isNotBlank()) {
            Text(
                notice,
                color = if (notice.startsWith("Please"))
                    MaterialTheme.colorScheme.error else AppBlue
            )
            Spacer(Modifier.height(10.dp))
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Publish Report", fontSize = 16.sp)
        }

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 7.dp)
    )
}

@Composable
fun ItemDetailsScreen(
    item: FoundItem,
    onBack: () -> Unit,
    onContact: () -> Unit,
    onReturned: () -> Unit,
    notice: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "‹  Back to items",
            color = AppBlue,
            modifier = Modifier.clickable(onClick = onBack),
            fontSize = 16.sp
        )

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(LightBlue, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 82.sp)
        }

        Spacer(Modifier.height(20.dp))
        Text(item.name, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Surface(
            color = LightBlue,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                item.type,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = AppBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(20.dp))
        DetailLine("Category", item.category)
        DetailLine("Location", item.location)
        DetailLine("Date reported", item.date)
        DetailLine("Posted by", item.contact)

        Spacer(Modifier.height(18.dp))
        Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(item.description, color = Muted, lineHeight = 23.sp)

        Spacer(Modifier.height(24.dp))

        if (item.type != "Returned") {
            Button(
                onClick = onContact,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Contact ${if (item.type == "Lost") "Owner" else "Finder"}")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onReturned,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Mark as Returned")
            }
        }

        if (notice.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(notice, color = AppBlue)
        }
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = Muted, modifier = Modifier.weight(1f))
        Text(
            value,
            modifier = Modifier.weight(1.2f),
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFFE7EAF0))
}

@Composable
fun ProfileScreen(
    myReports: List<FoundItem>,
    onItemClick: (FoundItem) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("My Profile", "Your Lost & Found activity")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(LightBlue, RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 29.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Prototype User",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Demo account", color = Muted, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "My Reports (${myReports.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (myReports.isEmpty()) {
            EmptyState(
                "No reports yet",
                "Items you report will appear here."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myReports, key = { it.id }) { item ->
                    ItemCard(item) { onItemClick(item) }
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔎", fontSize = 42.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(5.dp))
        Text(message, color = Muted)
    }
}
