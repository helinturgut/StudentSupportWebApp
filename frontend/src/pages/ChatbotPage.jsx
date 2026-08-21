import { useEffect, useRef, useState } from 'react';
import {
  createChatSession,
  deleteChatSession,
  downloadChatAttachment,
  getChatMessages,
  getChatSessions,
  sendChatMessage,
  uploadChatFile,
} from '../api/chat';
import { extractErrorMessage } from '../api/client';
import AiMarkdown from '../components/AiMarkdown';

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString('en-GB', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function ChatbotPage() {
  const [sessions, setSessions] = useState([]);
  const [activeSessionId, setActiveSessionId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [draft, setDraft] = useState('');
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);

  useEffect(() => {
    getChatSessions()
      .then((data) => {
        setSessions(data);
        if (data.length > 0) {
          selectSession(data[0].sessionId);
        }
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoadingSessions(false));
  }, []);

  const selectSession = (sessionId) => {
    setActiveSessionId(sessionId);
    setLoadingMessages(true);
    setError('');
    getChatMessages(sessionId)
      .then(setMessages)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoadingMessages(false));
  };

  const handleNewChat = () => {
    setError('');
    createChatSession()
      .then((session) => {
        setSessions((prev) => [session, ...prev]);
        setActiveSessionId(session.sessionId);
        setMessages([]);
      })
      .catch((err) => setError(extractErrorMessage(err)));
  };

  const refreshSessionOrder = () => {
    getChatSessions()
      .then(setSessions)
      .catch(() => {});
  };

  const ensureSession = () => {
    if (activeSessionId) return Promise.resolve(activeSessionId);
    return createChatSession().then((session) => {
      setSessions((prev) => [session, ...prev]);
      setActiveSessionId(session.sessionId);
      return session.sessionId;
    });
  };

  const handleSend = (e) => {
    e.preventDefault();
    const text = draft.trim();
    if (!text || sending) return;
    setSending(true);
    setError('');
    setDraft('');
    const tempId = `pending-${Date.now()}`;
    setMessages((prev) => [
      ...prev,
      { messageId: tempId, sender: 'USER', messageText: text, attachmentUrl: null, attachmentFileName: null },
    ]);
    ensureSession()
      .then((sessionId) => sendChatMessage(sessionId, text))
      .then((newMessages) => {
        setMessages((prev) => [...prev.filter((m) => m.messageId !== tempId), ...newMessages]);
        refreshSessionOrder();
      })
      .catch((err) => {
        setMessages((prev) => prev.filter((m) => m.messageId !== tempId));
        setDraft(text);
        setError(extractErrorMessage(err));
      })
      .finally(() => setSending(false));
  };

  const handleDeleteSession = (e, sessionId) => {
    e.stopPropagation();
    if (!window.confirm('Delete this chat? This cannot be undone.')) return;
    setError('');
    deleteChatSession(sessionId)
      .then(() => {
        setSessions((prev) => prev.filter((s) => s.sessionId !== sessionId));
        if (sessionId === activeSessionId) {
          setActiveSessionId(null);
          setMessages([]);
        }
      })
      .catch((err) => setError(extractErrorMessage(err)));
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file || sending) return;
    setSending(true);
    setError('');
    ensureSession()
      .then((sessionId) => uploadChatFile(sessionId, file))
      .then((message) => {
        setMessages((prev) => [...prev, message]);
        refreshSessionOrder();
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setSending(false));
  };

  return (
    <div className="page-shell page-chatbot">
      <h1>AI Chatbot</h1>
      <p className="alert alert-info">
        Responses are AI-generated and may be inaccurate — don't treat them as professional advice. Your
        conversations are stored so you can review your chat history.
      </p>
      {error && <p className="alert alert-error">{error}</p>}
      <div className="chatbot-layout">
        <aside className="chat-sidebar card">
          <button type="button" className="btn btn-primary chat-new-btn" onClick={handleNewChat}>
            + New Chat
          </button>
          <div className="chat-session-list">
            {loadingSessions ? (
              <p className="chat-empty">Loading chats…</p>
            ) : sessions.length === 0 ? (
              <p className="chat-empty">No previous chats yet.</p>
            ) : (
              sessions.map((s) => (
                <div
                  key={s.sessionId}
                  role="button"
                  tabIndex={0}
                  className={`chat-session-item${s.sessionId === activeSessionId ? ' active' : ''}`}
                  onClick={() => selectSession(s.sessionId)}
                  onKeyDown={(e) => e.key === 'Enter' && selectSession(s.sessionId)}
                >
                  <div className="chat-session-info">
                    <span className="chat-session-title">{s.title}</span>
                    <span className="chat-session-date">{formatDate(s.updatedAt)}</span>
                  </div>
                  <button
                    type="button"
                    className="chat-session-delete"
                    onClick={(e) => handleDeleteSession(e, s.sessionId)}
                    title="Delete chat"
                    aria-label="Delete chat"
                  >
                    🗑
                  </button>
                </div>
              ))
            )}
          </div>
        </aside>

        <div className="chat-window card">
          <div className="chat-messages">
            {loadingMessages ? (
              <p className="chat-empty">Loading messages…</p>
            ) : !activeSessionId ? (
              <p className="chat-empty">Start a new chat or say hello below.</p>
            ) : messages.length === 0 ? (
              <p className="chat-empty">No messages yet. Say hello below.</p>
            ) : (
              messages.map((m) => (
                <div key={m.messageId} className={`chat-bubble chat-bubble-${m.sender.toLowerCase()}`}>
                  {m.attachmentUrl ? (
                    <button
                      type="button"
                      className="chat-attachment"
                      onClick={() => downloadChatAttachment(m.messageId, m.attachmentFileName)}
                    >
                      📎 {m.attachmentFileName}
                    </button>
                  ) : m.sender === 'BOT' ? (
                    <AiMarkdown>{m.messageText}</AiMarkdown>
                  ) : (
                    m.messageText
                  )}
                </div>
              ))
            )}
            {sending && (
              <div className="chat-bubble chat-bubble-bot chat-bubble-typing">
                <span className="chat-typing-dot" />
                <span className="chat-typing-dot" />
                <span className="chat-typing-dot" />
              </div>
            )}
          </div>
          <form className="chat-input-bar" onSubmit={handleSend}>
            <input
              type="file"
              ref={fileInputRef}
              style={{ display: 'none' }}
              onChange={handleFileChange}
            />
            <button
              type="button"
              className="btn btn-ghost chat-upload-btn"
              onClick={() => fileInputRef.current?.click()}
              disabled={sending}
              title="Attach a file"
            >
              📎
            </button>
            <input
              type="text"
              placeholder="Type a message…"
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              disabled={sending}
            />
            <button type="submit" className="btn btn-primary" disabled={sending}>
              Send
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
