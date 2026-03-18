package project.team36.ui.map

import android.annotation.SuppressLint
import project.team36.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType


@SuppressLint("DefaultLocale")
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<SearchSuggestion>,
    onSuggestionClicked: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Search") },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.LightGray
        ),
        shape = RoundedCornerShape(8.dp)
    )

    if (suggestions.isNotEmpty()) {
        Surface(
            color = Color.White,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .heightIn(max = 300.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                val filteredSuggestions = suggestions.filter { it.type !is SearchSuggestionType.Category }

                filteredSuggestions.forEachIndexed { index, suggestion ->
                    // Get distance in kilometers if possible
                    val addressText = suggestion.fullAddress
                        ?: listOfNotNull(
                            suggestion.address?.region,
                            suggestion.address?.country
                        ).joinToString(", ")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClicked(suggestion) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search_result_marker),
                            contentDescription = "Mapbox Marker",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(24.dp)
                        )

                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = suggestion.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = addressText,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    if (index < filteredSuggestions.lastIndex) {
                        HorizontalDivider(color = Color.LightGray)
                    }
                }
            }
        }
    }
}
