(function() {
  'use strict';

  // 서버 URL (data-server 속성에서 가져오기)
  const script = document.currentScript;
  const SERVER_URL = script.getAttribute('data-server') || 'https://breaddesk.k6s.app';
  const API_BASE = SERVER_URL + '/api/v1/webchat';

  // 세션 정보
  let sessionId = sessionStorage.getItem('breaddesk_session_id');
  let sessionToken = sessionStorage.getItem('breaddesk_session_token');
  let pollingInterval = null;

  // 위젯 HTML 생성
  function createWidget() {
    const container = document.createElement('div');
    container.id = 'breaddesk-widget-container';
    container.innerHTML = `
      <style>
        #breaddesk-widget-container {
          position: fixed;
          bottom: 20px;
          right: 20px;
          z-index: 9999;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
        }
        
        #breaddesk-float-btn {
          width: 60px;
          height: 60px;
          border-radius: 30px;
          background: #3B82F6;
          color: white;
          border: none;
          box-shadow: 0 4px 12px rgba(0,0,0,0.15);
          cursor: pointer;
          font-size: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: transform 0.2s;
        }
        
        #breaddesk-float-btn:hover {
          transform: scale(1.1);
        }
        
        #breaddesk-chat-window {
          display: none;
          position: fixed;
          bottom: 90px;
          right: 20px;
          width: 380px;
          height: 550px;
          max-height: 80vh;
          background: white;
          border-radius: 12px;
          box-shadow: 0 8px 24px rgba(0,0,0,0.15);
          flex-direction: column;
          overflow: hidden;
        }
        
        #breaddesk-chat-window.open {
          display: flex;
        }
        
        .breaddesk-header {
          background: #3B82F6;
          color: white;
          padding: 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .breaddesk-header h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
        }
        
        .breaddesk-close-btn {
          background: none;
          border: none;
          color: white;
          font-size: 24px;
          cursor: pointer;
          padding: 0;
          width: 30px;
          height: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        
        .breaddesk-messages {
          flex: 1;
          overflow-y: auto;
          padding: 16px;
          background: #F9FAFB;
        }
        
        .breaddesk-message {
          margin-bottom: 12px;
          display: flex;
          flex-direction: column;
        }
        
        .breaddesk-message.user {
          align-items: flex-end;
        }
        
        .breaddesk-message.ai,
        .breaddesk-message.agent {
          align-items: flex-start;
        }
        
        .breaddesk-bubble {
          max-width: 75%;
          padding: 10px 14px;
          border-radius: 16px;
          word-wrap: break-word;
          white-space: pre-wrap;
        }
        
        .breaddesk-message.user .breaddesk-bubble {
          background: #3B82F6;
          color: white;
        }
        
        .breaddesk-message.ai .breaddesk-bubble,
        .breaddesk-message.agent .breaddesk-bubble {
          background: white;
          color: #1F2937;
          border: 1px solid #E5E7EB;
        }
        
        .breaddesk-timestamp {
          font-size: 11px;
          color: #9CA3AF;
          margin-top: 4px;
        }
        
        .breaddesk-input-area {
          padding: 12px;
          border-top: 1px solid #E5E7EB;
          background: white;
        }
        
        .breaddesk-input-wrapper {
          display: flex;
          gap: 8px;
        }
        
        .breaddesk-input {
          flex: 1;
          padding: 10px 12px;
          border: 1px solid #D1D5DB;
          border-radius: 8px;
          font-size: 14px;
          outline: none;
        }
        
        .breaddesk-input:focus {
          border-color: #3B82F6;
        }
        
        .breaddesk-send-btn {
          padding: 10px 16px;
          background: #3B82F6;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 500;
        }
        
        .breaddesk-send-btn:hover {
          background: #2563EB;
        }
        
        .breaddesk-send-btn:disabled {
          background: #9CA3AF;
          cursor: not-allowed;
        }
        
        .breaddesk-welcome {
          padding: 20px;
          text-align: center;
        }
        
        .breaddesk-welcome h4 {
          margin: 0 0 16px 0;
          color: #1F2937;
        }
        
        .breaddesk-form-group {
          margin-bottom: 12px;
          text-align: left;
        }
        
        .breaddesk-form-group label {
          display: block;
          margin-bottom: 4px;
          font-size: 13px;
          color: #6B7280;
        }
        
        .breaddesk-form-group input {
          width: 100%;
          padding: 8px 12px;
          border: 1px solid #D1D5DB;
          border-radius: 6px;
          font-size: 14px;
        }
        
        .breaddesk-start-btn {
          width: 100%;
          padding: 12px;
          background: #3B82F6;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 500;
          margin-top: 8px;
        }
        
        .breaddesk-start-btn:hover {
          background: #2563EB;
        }
        
        @media (max-width: 480px) {
          #breaddesk-chat-window {
            width: calc(100vw - 40px);
            height: calc(100vh - 120px);
            bottom: 90px;
          }
        }
      </style>
      
      <button id="breaddesk-float-btn" aria-label="채팅 열기">🍞</button>
      
      <div id="breaddesk-chat-window">
        <div class="breaddesk-header">
          <h3>BreadDesk 고객 지원</h3>
          <button class="breaddesk-close-btn" aria-label="닫기">×</button>
        </div>
        
        <div id="breaddesk-welcome-screen" class="breaddesk-welcome">
          <h4>안녕하세요! 👋</h4>
          <p style="color: #6B7280; font-size: 14px; margin-bottom: 20px;">
            무엇을 도와드릴까요?
          </p>
          <div class="breaddesk-form-group">
            <label>이름 (선택)</label>
            <input type="text" id="breaddesk-name-input" placeholder="홍길동">
          </div>
          <div class="breaddesk-form-group">
            <label>이메일 (선택)</label>
            <input type="email" id="breaddesk-email-input" placeholder="example@email.com">
          </div>
          <button class="breaddesk-start-btn" id="breaddesk-start-chat">채팅 시작</button>
        </div>
        
        <div id="breaddesk-chat-screen" style="display: none; flex: 1; display: flex; flex-direction: column;">
          <div class="breaddesk-messages" id="breaddesk-messages"></div>
          <div class="breaddesk-input-area">
            <div class="breaddesk-input-wrapper">
              <input type="text" class="breaddesk-input" id="breaddesk-message-input" placeholder="메시지를 입력하세요...">
              <button class="breaddesk-send-btn" id="breaddesk-send-btn">전송</button>
            </div>
          </div>
        </div>
      </div>
    `;
    
    document.body.appendChild(container);
    
    // 이벤트 리스너
    const floatBtn = document.getElementById('breaddesk-float-btn');
    const chatWindow = document.getElementById('breaddesk-chat-window');
    const closeBtn = chatWindow.querySelector('.breaddesk-close-btn');
    const startBtn = document.getElementById('breaddesk-start-chat');
    const sendBtn = document.getElementById('breaddesk-send-btn');
    const messageInput = document.getElementById('breaddesk-message-input');
    
    floatBtn.addEventListener('click', () => {
      chatWindow.classList.toggle('open');
      if (chatWindow.classList.contains('open') && sessionId) {
        loadMessages();
      }
    });
    
    closeBtn.addEventListener('click', () => {
      chatWindow.classList.remove('open');
    });
    
    startBtn.addEventListener('click', startChat);
    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });
    
    // 기존 세션 복원
    if (sessionId) {
      showChatScreen();
    }
  }

  async function startChat() {
    const name = document.getElementById('breaddesk-name-input').value.trim() || '웹챗 사용자';
    const email = document.getElementById('breaddesk-email-input').value.trim() || null;
    
    try {
      const response = await fetch(`${API_BASE}/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ senderName: name, senderEmail: email })
      });
      
      if (!response.ok) throw new Error('세션 생성 실패');
      
      const data = await response.json();
      sessionId = data.sessionId;
      sessionToken = data.token;
      
      sessionStorage.setItem('breaddesk_session_id', sessionId);
      sessionStorage.setItem('breaddesk_session_token', sessionToken);
      
      showChatScreen();
    } catch (error) {
      console.error('세션 생성 오류:', error);
      alert('채팅 세션을 시작할 수 없습니다. 다시 시도해주세요.');
    }
  }

  function showChatScreen() {
    document.getElementById('breaddesk-welcome-screen').style.display = 'none';
    document.getElementById('breaddesk-chat-screen').style.display = 'flex';
    loadMessages();
    startPolling();
  }

  async function sendMessage() {
    const input = document.getElementById('breaddesk-message-input');
    const message = input.value.trim();
    
    if (!message || !sessionId) return;
    
    input.value = '';
    const sendBtn = document.getElementById('breaddesk-send-btn');
    sendBtn.disabled = true;
    
    // 사용자 메시지 즉시 표시
    addMessage('user', message, new Date().toISOString());
    
    try {
      const response = await fetch(`${API_BASE}/sessions/${sessionId}/messages`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message })
      });
      
      if (!response.ok) throw new Error('메시지 전송 실패');
      
      const data = await response.json();
      
      // AI 응답이 있으면 즉시 표시
      if (data.aiResponse) {
        addMessage('ai', data.aiResponse, new Date().toISOString());
      }
    } catch (error) {
      console.error('메시지 전송 오류:', error);
      addMessage('ai', '죄송합니다. 메시지 전송에 실패했습니다.', new Date().toISOString());
    } finally {
      sendBtn.disabled = false;
      input.focus();
    }
  }

  async function loadMessages() {
    if (!sessionId) return;
    
    try {
      const response = await fetch(`${API_BASE}/sessions/${sessionId}/messages`);
      if (!response.ok) return;
      
      const messages = await response.json();
      const container = document.getElementById('breaddesk-messages');
      container.innerHTML = '';
      
      messages.forEach(msg => {
        addMessage(msg.role.toLowerCase(), msg.message, msg.createdAt, false);
      });
    } catch (error) {
      console.error('메시지 로드 오류:', error);
    }
  }

  function addMessage(role, text, timestamp, scroll = true) {
    const container = document.getElementById('breaddesk-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `breaddesk-message ${role}`;
    
    const bubble = document.createElement('div');
    bubble.className = 'breaddesk-bubble';
    bubble.textContent = text;
    
    const time = document.createElement('div');
    time.className = 'breaddesk-timestamp';
    time.textContent = formatTime(timestamp);
    
    messageDiv.appendChild(bubble);
    messageDiv.appendChild(time);
    container.appendChild(messageDiv);
    
    if (scroll) {
      container.scrollTop = container.scrollHeight;
    }
  }

  function formatTime(isoString) {
    const date = new Date(isoString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  function startPolling() {
    if (pollingInterval) clearInterval(pollingInterval);
    
    pollingInterval = setInterval(async () => {
      if (!sessionId) return;
      
      try {
        const response = await fetch(`${API_BASE}/sessions/${sessionId}/messages`);
        if (!response.ok) return;
        
        const messages = await response.json();
        const container = document.getElementById('breaddesk-messages');
        const currentCount = container.children.length;
        
        if (messages.length > currentCount) {
          // 새 메시지가 있으면 추가
          for (let i = currentCount; i < messages.length; i++) {
            const msg = messages[i];
            addMessage(msg.role.toLowerCase(), msg.message, msg.createdAt);
          }
        }
      } catch (error) {
        console.error('폴링 오류:', error);
      }
    }, 30000); // 30초마다 폴링
  }

  // 위젯 초기화
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', createWidget);
  } else {
    createWidget();
  }
})();
