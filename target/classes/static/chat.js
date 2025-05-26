let socket = null;

const chatLog = document.getElementById('chatLog');
const userCountSpan = document.getElementById('userCount');
const userListUl = document.getElementById('userList');
const messageInput = document.getElementById('messageInput');
const usernameInput = document.getElementById('username');
const sendBtn = document.getElementById('sendBtn');
const connectBtn = document.getElementById('connectBtn');

function connect() {
  const username = usernameInput.value.trim();
  if (!username) {
    alert("Por favor, ingresa tu nombre antes de conectarte.");
    return;
  }

  socket = new WebSocket('ws://' + window.location.host + '/chat');

  socket.onopen = () => {
    console.log("Conectado al servidor WebSocket");
    socket.send(JSON.stringify({ user: username }));

    // Bloquear input nombre y botón conectar
    usernameInput.disabled = true;
    connectBtn.disabled = true;

    // Habilitar input mensaje y botón enviar
    messageInput.disabled = false;
    sendBtn.disabled = false;
    messageInput.focus();
  };

  socket.onmessage = (event) => {
    const msg = JSON.parse(event.data);

    if (msg.type === 'system') {
      displaySystemMessage(msg.message);
      updateUserCount(msg.userCount);

      if (msg.users) {
        updateUserList(msg.users);
      }
    } else {
      displayMessage(msg.user, msg.message);
    }
  };

  socket.onclose = () => {
    console.log("Conexión WebSocket cerrada");
    displaySystemMessage("Conexión al servidor perdida.");
    updateUserCount(0);
    updateUserList([]);

    // Volver a habilitar para reconectar
    usernameInput.disabled = false;
    connectBtn.disabled = false;
    messageInput.disabled = true;
    sendBtn.disabled = true;
  };

  socket.onerror = (error) => {
    console.error("Error en WebSocket:", error);
  };
}

function sendMessage() {
  const message = messageInput.value.trim();
  const username = usernameInput.value.trim();

  if (message === '') return;

  if (socket && socket.readyState === WebSocket.OPEN) {
    const msgObj = {
      user: username,
      message: message
    };

    socket.send(JSON.stringify(msgObj));
    messageInput.value = '';
  } else {
    alert("La conexión no está abierta.");
  }
}

function displayMessage(user, message) {
  const div = document.createElement('div');
  div.classList.add('message');

  const userSpan = document.createElement('strong');
  userSpan.textContent = user + ": ";
  div.appendChild(userSpan);

  const msgSpan = document.createElement('span');
  msgSpan.textContent = message;
  div.appendChild(msgSpan);

  chatLog.appendChild(div);
  chatLog.scrollTop = chatLog.scrollHeight;
}

function displaySystemMessage(message) {
  if (!message) return;

  const div = document.createElement('div');
  div.classList.add('system-message');
  div.textContent = message;

  chatLog.appendChild(div);
  chatLog.scrollTop = chatLog.scrollHeight;
}

function updateUserCount(count) {
  userCountSpan.textContent = count;
}

function updateUserList(users) {
  userListUl.innerHTML = '';
  users.forEach(user => {
    const li = document.createElement('li');
    li.classList.add('list-group-item', 'py-1');
    li.textContent = user;
    userListUl.appendChild(li);
  });
}

sendBtn.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') {
    sendMessage();
  }
});

connectBtn.addEventListener('click', connect);
