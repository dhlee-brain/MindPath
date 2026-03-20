package com.example.mindpath.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindpath.ui.components.DialStartButton_Ritual
import com.example.mindpath.ui.components.PickHourMinuteSecondFun
import com.example.mindpath.viewmodel.MeditationViewModel
import com.example.mindpath.viewmodel.TimerViewModel
import kotlin.math.atan2


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialStartScreen(
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel(),
    meditationViewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory)
) {
    val isRunning by timerViewModel.isTimerRunning.collectAsState()
    val timeLeft by timerViewModel.timeLeft.collectAsState()
    var openDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DialStartButton_Ritual(
            durationSeconds = timeLeft,
            isRunning = isRunning,
            onStart = {
                meditationViewModel.startMeditation()
                timerViewModel.startTimer(onFinish = {
                    meditationViewModel.finishMeditation("Good")
                })
            },
            onTouchDuringRunning = {
                meditationViewModel.addTouchRecord()
            }
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        TimerDisplay(timeLeft = timeLeft, isRunning = isRunning)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (!isRunning) {
            Row {
                Spacer(modifier = Modifier.width(100.dp))
                Button(
                    onClick = { openDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF41C3E7),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                ) {
                    Text(fontSize = 20.sp, text = "시간 선택")
                }
            }
        }
        
        if (openDialog) {
            BasicAlertDialog(
                onDismissRequest = { openDialog = false }
            ) {
                Surface(
                    modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PickHourMinuteSecondFun(
                            initialTotalSeconds = timeLeft,
                            onTimeChange = { newSeconds ->
                                timerViewModel.setTime(newSeconds)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(
                            onClick = { openDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(timeLeft: Int, isRunning: Boolean) {
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
            style = MaterialTheme.typography.displayLarge,
            color = Color.Companion.DarkGray
        )
        if (isRunning) {
            Text(text = "Focusing...", color = Color.Companion.Gray)
        }
    }
}

fun angleDeg(v: Offset): Float {
    val rad = atan2(v.y, v.x)
    return (rad * 180f / Math.PI.toFloat())
}

fun smallestAngleDeltaDeg(from: Float, to: Float): Float {
    var delta = to - from
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f
    return delta
}
