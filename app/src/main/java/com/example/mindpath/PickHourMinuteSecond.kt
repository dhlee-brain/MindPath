package com.example.mindpath

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.PickHourMinuteSecond
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle

@Composable
fun PickHourMinuteSecondFun() {
    var hour by remember { mutableStateOf(1) }
    var minute by remember { mutableStateOf(10)}
    var second by remember { mutableStateOf(15)}
    PickHourMinuteSecond(
        initialHour = hour,
        onHourChange = { hour = it },
        initialMinute = minute,
        onMinuteChange = { minute = it },
        initialSecond = second,
        onSecondChange = { second = it },
        focusIndicator = PickTimeFocusIndicator(
            enabled = true,
            widthFull = true,
            background = Color(0xFFE1D8FF),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(5.dp, Color(0xFF87CDE6)),
        )
    )
}

@Composable
fun PickHourMinuteFun() {
    var hour by remember { mutableIntStateOf(0) }
    var minute by remember { mutableIntStateOf(0) }



    PickHourMinute(
        initialHour = hour,
        onHourChange = { hour = it },
        initialMinute = minute,
        onMinuteChange = { minute = it },
        selectedTextStyle = PickTimeTextStyle(
            color = Color(0xFF5F5BAC),
            fontSize = 26.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
        ),
        unselectedTextStyle = PickTimeTextStyle(
            color = Color(0xFFAEABE3),
            fontSize = 26.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
        ),
        verticalSpace = 10.dp,
        horizontalSpace = 15.dp,
        containerColor = Color.White,
        isLooping = false,
        extraRow = 2,
        focusIndicator = PickTimeFocusIndicator(
            enabled = true,
            widthFull = true,
            background = Color(0xFFE1D8FF),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(0.dp, Color(0xFF87CDE6)),
        )
    )
}