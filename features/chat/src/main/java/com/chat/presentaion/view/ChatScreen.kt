package com.chat.presentaion.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chat.R
import com.chat.entity.Message
import com.chat.entity.isMe
import com.chat.presentaion.viewmodel.ChatState
import com.chat.presentaion.viewmodel.ChatViewModel
import com.core.extension.formatDateToHhMm
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.background(Color.White),
        topBar = { ChatTopBar() },
        content = { ChatContent(state = state, paddingValues = it) },
        bottomBar = { ChatBottomBar(sendMsg = { msg -> viewModel.sendMessage(msg) }) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar() {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.lbl_chat_demo),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        navigationIcon = {
            val activity = LocalContext.current as? ComponentActivity
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        activity?.onBackPressedDispatcher?.onBackPressed()
                    }
            )
        },
        actions = {
            Icon(
                painter = painterResource(R.drawable.ic_call),
                contentDescription = "Call",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { }
            )
        }
    )
}

@Composable
fun ChatContent(
    state: ChatState,
    paddingValues: PaddingValues
) {
    when (state) {
        is ChatState.HistoryLoaded -> MessageList(
            list = state.date,
            paddingValues = paddingValues
        )

        is ChatState.MessageReceived -> MessageList(
            list = state.date,
            paddingValues = paddingValues
        )

        is ChatState.MessageSent -> MessageList(
            list = state.date,
            paddingValues = paddingValues,
            scrollToEnd = true
        )

        is ChatState.Loading -> Loading()
        is ChatState.NoHistoryCached -> EmptyView(message = stringResource(id = R.string.lbl_there_no_messages))
        else -> Loading()
    }
}

@Composable
fun MessageList(
    list: List<Message>,
    paddingValues: PaddingValues,
    scrollToEnd: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyColumnListState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        reverseLayout = true,
        state = lazyColumnListState
    ) {
        items(list.size) { index ->
            ChatMessage(message = list[index])
        }

        coroutineScope.launch { if (scrollToEnd) lazyColumnListState.scrollToItem(0) }
    }
}

@Composable
fun ChatMessage(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe()) Arrangement.End else Arrangement.Start
    ) {
        Column {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 40f,
                            bottomEnd = 40f,
                            topStart = if (message.isMe()) 40f else 0f,
                            topEnd = if (message.isMe()) 0f else 40f
                        )
                    )
                    .background(if (message.isMe()) colorResource(R.color.gray) else Color.Black)
            ) {
                Text(
                    text = message.text,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp),
                    color = if (message.isMe()) Color.Black else Color.White
                )
            }

            Text(
                text = message.dateTime.formatDateToHhMm(),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                color = colorResource(id = R.color.light_gray)
            )
        }

    }
}


@Composable
fun ChatBottomBar(sendMsg: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        var text by remember { mutableStateOf("") }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.lbl_send_msg_hint)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.isNotEmpty()) {
                            sendMsg(text.trim())
                            text = ""
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "Send",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun Loading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color.Black
        )
    }
}

@Composable
fun EmptyView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = message,
            modifier = Modifier
                .size(40.dp)
        )
    }
}

@Preview
@Composable
private fun EmptyViewPreview() {
    EmptyView(message = stringResource(id = R.string.lbl_there_no_messages))
}