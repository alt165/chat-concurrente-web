let socket;
const messagesDiv = document.getElementById('messages');

function connect() {
  // Establecer la conexión WebSocket (ajustar puerto si no es 8080)
  socket = new WebSocket('ws://' + window.location.host + '/chat');

  socket.onopen = () => {
    console.log("Conectado al servidor WebSocket");
  };

  socket.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    displayMessage(msg.user, msg.message);
  };

  socket.onclose = () => {
    displaySystemMessage("Desconectado del servidor");
  };

  socket.onerror = (err) => {
    console.error("WebSocket error", err);
  };
}

function displayMessage(user, message) {
  const div = document.createElement('div');
  div.textContent = `${user}: ${message}`;
  messagesDiv.appendChild(div);
  messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function displaySystemMessage(message) {
  const div = document.createElement('div');
  div.style.color = "gray";
  div.textContent = `[Sistema] ${message}`;
  messagesDiv.appendChild(div);
  messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function sendMessage() {
  const username = document.getElementById('username').value.trim();
  const message = document.getElementById('message').value.trim();

  if (!username || !message || socket.readyState !== WebSocket.OPEN) return;

  const msgObj = {
    user: username,
    message: message
  };

  socket.send(JSON.stringify(msgObj));
  document.getElementById('message').value = '';
}

// Conectar automáticamente al cargar la página
window.addEventListener('load', connect);
