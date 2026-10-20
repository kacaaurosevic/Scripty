import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import client from "../api/client";
import UploadForm from "../components/UploadForm";

const STATUS_LABEL = {
  PENDING: "Queued",
  GENERATING: "Generating...",
  READY: "Ready",
  FAILED: "Failed",
};

export default function Dashboard() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDocuments();
  }, []);

  async function loadDocuments() {
    try {
      const { data } = await client.get("/documents");
      setDocuments(data);
    } finally {
      setLoading(false);
    }
  }
    async function deleteDocument(id, title) {
    if (!window.confirm(`Delete "${title}"? This can't be undone.`)) return;
    try {
      await client.delete(`/documents/${id}`);
      setDocuments((docs) => docs.filter((d) => d.id !== id));
    } catch {
      alert("Could not delete this material. Please try again.");
    }
  }

  return (
    <div className="dashboard">
      <UploadForm onCreated={() => loadDocuments()} />

      <section className="history-section">
        <h2>Your study materials</h2>
        {loading && <p>Loading...</p>}
        {!loading && documents.length === 0 && <p className="muted">Nothing here yet — upload your first material above.</p>}

        <ul className="history-list">
          {documents.map((doc) => (
                        <li key={doc.id} className="history-row">
              <Link to={`/documents/${doc.id}`} className="history-item">
                <span className="history-title">{doc.title}</span>
                <span className={`status-pill status-${doc.status.toLowerCase()}`}>
                  {STATUS_LABEL[doc.status] || doc.status}
                </span>
              </Link>
              <button
                type="button"
                className="delete-button"
                onClick={() => deleteDocument(doc.id, doc.title)}
                title="Delete"
                aria-label={`Delete ${doc.title}`}
              >
                ✕
              </button>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
