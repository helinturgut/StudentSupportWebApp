import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

export default function AiMarkdown({ children }) {
  return (
    <div className="ai-markdown">
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{children}</ReactMarkdown>
    </div>
  );
}
