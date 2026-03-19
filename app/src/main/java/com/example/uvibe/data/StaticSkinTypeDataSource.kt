package com.example.uvibe.data // 或者 repository

import androidx.compose.ui.graphics.Color
import com.example.uvibe.ui.model.SkinTypeModel // 引入你的 model

val skinTypeData = listOf(
    SkinTypeModel(0, Color(0xFFFFE0D0), "Very Fair", "Burns easily, tans minimally", "Approximately 10-15 minutes", "SPF 50+", 60,listOf(
        "Reapply sunscreen every 1 hour during peak UV hours",
        "Wear protective clothing and a wide-brimmed hat",
        "Seek shade between 10am-4pm",
        "Use sunscreens labeled \"broad-spectrum\""
    )),
    SkinTypeModel(1, Color(0xFFFFD1B1), "Fair", "Burns easily, tans minimally", "Approximately 15-20 minutes", "SPF 50+", 75,listOf(
        "Reapply sunscreen every 2 hours",
        "Wear protective clothing and a wide-brimmed hat",
        "Seek shade during peak UV hours (10am-4pm)",
        "Protect your eyes with UV-blocking sunglasses"
    )),
    SkinTypeModel(2, Color(0xFFFFC08F), "Light", "Burns moderately, tans gradually", "Approximately 20-25 minutes", "SPF 30+", 100,listOf(
        "Reapply sunscreen every 2-3 hours",
        "Seek shade during midday when UV is strongest",
        "Use a broad-spectrum sunscreen with high SPF",
        "Wear a hat and sunglasses outdoors"
    )),
    SkinTypeModel(3, Color(0xFFFFAD6F), "Medium", "Burns minimally, always tans well", "Approximately 25-30 minutes", "SPF 30+", 120,listOf(
        "Reapply sunscreen after swimming or sweating",
        "Use a broad-spectrum sunscreen",
        "Cover exposed skin with clothing",
        "Minimize midday sun exposure"
    )),
    SkinTypeModel(4, Color(0xFFC88F5A), "Tan", "Rarely burns, tans quickly", "Approximately 30-35 minutes", "SPF 30+", 120,listOf(
        "Use a broad-spectrum sunscreen",
        "Cover up with clothing",
        "Seek shade when possible",
        "Limit prolonged sun exposure"
    )),
    SkinTypeModel(5, Color(0xFFA5734A), "Deep", "Never burns, always tans deeply", "Approximately 35-40 minutes", "SPF 15+", 120,listOf(
        "Use a broad-spectrum sunscreen",
        "Seek shade when possible",
        "Protect your skin, even with a tan",
        "Apply sunscreen generously"
    ))
)