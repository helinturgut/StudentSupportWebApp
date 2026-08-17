import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import Reveal from '../components/Reveal';
import iconLight from '../assets/icon-light.png';
import iconDark from '../assets/icon-dark.png';
import landingBackground from '../assets/landing-background.png';

function useParallax(speed = 0.15, maxOffset = 90) {
  const ref = useRef(null);

  useEffect(() => {
    const node = ref.current;
    if (!node) return undefined;
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return undefined;

    let ticking = false;
    const update = () => {
      const offset = Math.min(window.scrollY * speed, maxOffset);
      node.style.transform = `translateY(${offset}px)`;
      ticking = false;
    };
    const onScroll = () => {
      if (!ticking) {
        ticking = true;
        requestAnimationFrame(update);
      }
    };

    update();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, [speed, maxOffset]);

  return ref;
}

function Icon({ path, ...props }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      {path}
    </svg>
  );
}

const ICONS = {
  chat: (
    <path d="M4 5.5A1.5 1.5 0 0 1 5.5 4h13A1.5 1.5 0 0 1 20 5.5v10a1.5 1.5 0 0 1-1.5 1.5H10l-4.5 4v-4H5.5A1.5 1.5 0 0 1 4 15.5z" />
  ),
  cv: (
    <>
      <path d="M7 3h7l4 4v13a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z" />
      <path d="M14 3v4h4" />
      <path d="m9.5 14 1.8 1.8L14.5 12" />
    </>
  ),
  interview: (
    <>
      <rect x="9" y="2.5" width="6" height="10.5" rx="3" />
      <path d="M6 11v1a6 6 0 0 0 12 0v-1" />
      <path d="M12 18v3.5M9 21.5h6" />
    </>
  ),
  pathway: (
    <>
      <circle cx="5.5" cy="6" r="2" />
      <circle cx="18.5" cy="18" r="2" />
      <path d="M5.5 8v3a3 3 0 0 0 3 3h7a3 3 0 0 1 3 3v1" />
    </>
  ),
  resources: (
    <path d="M4 4.5A1.5 1.5 0 0 1 5.5 3H11v18H5.5A1.5 1.5 0 0 1 4 19.5ZM20 4.5A1.5 1.5 0 0 0 18.5 3H13v18h5.5a1.5 1.5 0 0 0 1.5-1.5Z" />
  ),
  reminder: (
    <>
      <path d="M18 16v-4.5a6 6 0 1 0-12 0V16l-1.5 2.5h15Z" />
      <path d="M10 20.5a2 2 0 0 0 4 0" />
    </>
  ),
};

const FEATURES = [
  {
    icon: 'chat',
    title: 'AI Career Chatbot',
    description: 'Get instant answers to career questions from an AI assistant available whenever you need guidance.',
  },
  {
    icon: 'cv',
    title: 'CV Feedback',
    description: 'Upload your CV and receive tailored, actionable feedback to help it stand out to employers.',
  },
  {
    icon: 'interview',
    title: 'Interview Preparation',
    description: 'Practice with curated interview questions across roles and difficulty levels to build confidence.',
  },
  {
    icon: 'pathway',
    title: 'Career Pathways',
    description: 'Explore structured pathways that map out the skills and steps needed to reach your career goals.',
  },
  {
    icon: 'resources',
    title: 'Curated Resources',
    description: 'Browse a library of guides and resources selected to support your job search and development.',
  },
  {
    icon: 'reminder',
    title: 'Reminders',
    description: 'Stay on top of deadlines and next steps with reminders tailored to your career journey.',
  },
];

const HIGHLIGHTS = ['AI-powered guidance', 'Built for students', 'Free to get started'];

export default function LandingPage() {
  const parallaxRef = useParallax(0.18);

  return (
    <div className="landing-page">
      <div
        ref={parallaxRef}
        className="landing-parallax-bg"
        style={{ backgroundImage: `url(${landingBackground})` }}
        aria-hidden="true"
      />

      <header className="landing-header">
        <div className="app-brand">
          <img src={iconLight} alt="" className="app-brand-logo app-brand-logo-light" />
          <img src={iconDark} alt="" className="app-brand-logo app-brand-logo-dark" />
          <span className="app-brand-text">Student Support</span>
        </div>
        <div className="landing-header-actions">
          <Link to="/login" className="btn landing-btn-text">
            Log in
          </Link>
          <Link to="/register" className="btn landing-btn-pill">
            Get started
          </Link>
        </div>
      </header>

      <main className="landing-hero">
        <span className="landing-eyebrow">Student Support &amp; Career Advisor</span>
        <h1 className="landing-title">
          Your career journey, <span className="landing-title-accent">guided every step</span> of the way
        </h1>
        <p className="landing-subtitle">
          Prepare for interviews, polish your CV, and plan your career path with AI-powered guidance built
          specifically for students.
        </p>
        <div className="landing-hero-actions">
          <Link to="/register" className="btn landing-btn-hero-primary">
            Get started free
            <span className="landing-btn-arrow" aria-hidden="true">→</span>
          </Link>
          <a href="#features" className="btn landing-btn-hero-ghost">
            See what's included
          </a>
        </div>
        <ul className="landing-highlights">
          {HIGHLIGHTS.map((item) => (
            <li key={item}>
              <Icon path={<path d="m5 12 4.5 4.5L19 7" />} className="landing-highlight-icon" />
              {item}
            </li>
          ))}
        </ul>
      </main>

      <section id="features" className="landing-features">
        <Reveal>
          <span className="landing-eyebrow landing-eyebrow-center">What you get</span>
          <h2 className="landing-features-title">Everything you need to move forward</h2>
          <p className="landing-features-subtitle">
            One place to prepare, practice, and plan &mdash; with support tailored to where you are in your career
            journey.
          </p>
        </Reveal>
        <div className="landing-feature-grid">
          {FEATURES.map((feature, index) => (
            <Reveal
              key={feature.title}
              as="div"
              delay={index * 80}
              className="card landing-feature-card"
            >
              <div className="landing-feature-icon">
                <Icon path={ICONS[feature.icon]} />
              </div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </Reveal>
          ))}
        </div>
      </section>

      <Reveal as="section" className="landing-cta-band">
        <h2>Ready to take the next step?</h2>
        <p>Create your free account and start preparing for what's next.</p>
        <Link to="/register" className="btn landing-btn-hero-primary">
          Get started free
          <span className="landing-btn-arrow" aria-hidden="true">→</span>
        </Link>
      </Reveal>

      <footer className="landing-footer">
        <div className="app-brand">
          <img src={iconLight} alt="" className="app-brand-logo app-brand-logo-light" />
          <img src={iconDark} alt="" className="app-brand-logo app-brand-logo-dark" />
          <span className="app-brand-text">Student Support</span>
        </div>
        <p>© {new Date().getFullYear()} Student Support &amp; Career Advisor</p>
      </footer>
    </div>
  );
}
