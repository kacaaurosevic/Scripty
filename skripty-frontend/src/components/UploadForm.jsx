import { useState } from "react";
import { useNavigate } from "react-router-dom";
import client from "../api/client";

export default function UploadForm({ onCreated }) {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [file, setFile] = useState(null);
  const [pastedText, setPastedText] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!title.trim()) {
      setError("Please give your material a title.");
      return;
    }
    if (!file && !pastedText.trim()) {
      setError("Choose a file or paste some text.");
      return;
    }
    if (file && pastedText.trim()) {
      setError("Use either a file or pasted text, not both.");
      return;
    }

    setSubmitting(true);
    try {
      let response;
      if (file) {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("title", title);
        response = await client.post("/documents/upload", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      } else {
        response = await client.post("/documents/paste", { title, text: pastedText });
      }

      onCreated?.(response.data);
      navigate(`/documents/${response.data.id}`);
    } catch (err) {
      setError(err.response?.data?.error || "Something went wrong. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="upload-card" onSubmit={handleSubmit}>
      <h2>Add study material</h2>

      {error && <div className="error-banner">{error}</div>}

      <label>
        Title
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="e.g. Chapter 4 — Relational Databases"
          required
        />
      </label>

      <label>
        Upload a file (.pdf, .docx or .txt)
        <input
          type="file"
          accept=".pdf,.docx,.txt"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
      </label>

      <div className="or-divider">
        <span>or</span>
      </div>

      <label>
        Paste your notes
        <textarea
          rows={6}
          value={pastedText}
          onChange={(e) => setPastedText(e.target.value)}
          placeholder="Paste the material you want turned into a summary, test and flashcards..."
        />
      </label>

      <button type="submit" disabled={submitting}>
        {submitting ? "Uploading..." : "Generate study material"}
      </button>
    </form>
  );
}