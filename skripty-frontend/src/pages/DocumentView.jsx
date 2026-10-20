import { useEffect, useRef, useState } from "react";
import { useParams, Link } from "react-router-dom";
import client from "../api/client";
import FlashcardViewer from "../components/FlashcardViewer";
import TestViewer from "../components/TestViewer";

const POLL_INTERVAL_MS = 2500;

export default function DocumentView() {
  const { id } = useParams();
  const [doc, setDoc] = useState(null);
  const [tab, setTab] = useState("summary");
  const [error, setError] = useState("");
  const intervalRef = useRef(null);

  useEffect(() => {
    fetchDocument();
    intervalRef.current = setInterval(fetchDocument, POLL_INTERVAL_MS);
    return () => clearInterval(intervalRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function fetchDocument() {
    try {
      const { data } = await client.get(`/documents/${id}`);
      setDoc(data);
      if (data.status === "READY" || data.status === "FAILED") {
        clearInterval(intervalRef.current);
      }
    } catch (err) {
      setError(err.response?.data?.error || "Could not load this document.");
      clearInterval(intervalRef.current);
    }
  }

  if (error) {
    return (
      <div className="document-view">
        <Link to="/">&larr; Back</Link>
        <p className="error-banner">{error}</p>
      </div>
    );
  }

  if (!doc) {
    return <p className="muted">Loading...</p>;
  }

  if (doc.status === "PENDING" || doc.status === "GENERATING") {
    return (
      <div className="document-view">
        <Link to="/">&larr; Back</Link>
        <h1>{doc.title}</h1>
        <div className="generating-state">
          <div className="spinner" />
          <p>Generating your summary, test and flashcards...</p>
          <p className="muted small">This usually takes 10-30 seconds.</p>
        </div>
      </div>
    );
  }

  if (doc.status === "FAILED") {
    return (
      <div className="document-view">
        <Link to="/">&larr; Back</Link>
        <h1>{doc.title}</h1>
        <p className="error-banner">Generation failed. Please try uploading this material again.</p>
      </div>
    );
  }

  const testQuestions = doc.testQuestionsJson ? JSON.parse(doc.testQuestionsJson) : [];
  const flashcards = doc.flashcardsJson ? JSON.parse(doc.flashcardsJson) : [];

  return (
    <div className="document-view">
      <Link to="/">&larr; Back</Link>
      <h1>{doc.title}</h1>

      <div className="tabs">
        <button className={tab === "summary" ? "active" : ""} onClick={() => setTab("summary")}>
          Summary
        </button>
        <button className={tab === "test" ? "active" : ""} onClick={() => setTab("test")}>
          Test
        </button>
        <button className={tab === "flashcards" ? "active" : ""} onClick={() => setTab("flashcards")}>
          Flashcards
        </button>
      </div>

      <div className="tab-content">
        {tab === "summary" && <div className="summary-text">{doc.summaryText}</div>}
        {tab === "test" && <TestViewer questions={testQuestions} />}
        {tab === "flashcards" && <FlashcardViewer flashcards={flashcards} />}
      </div>
    </div>
  );
}
