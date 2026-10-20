import { useState } from "react";

export default function FlashcardViewer({ flashcards }) {
  const [index, setIndex] = useState(0);
  const [flipped, setFlipped] = useState(false);

  if (!flashcards || flashcards.length === 0) {
    return <p className="muted">No flashcards generated.</p>;
  }

  const card = flashcards[index];

  function next() {
    setFlipped(false);
    setIndex((i) => (i + 1) % flashcards.length);
  }

  function prev() {
    setFlipped(false);
    setIndex((i) => (i - 1 + flashcards.length) % flashcards.length);
  }

  return (
    <div className="flashcard-viewer">
      <p className="flashcard-counter">
        {index + 1} / {flashcards.length}
      </p>

      <div className={`flashcard ${flipped ? "flipped" : ""}`} onClick={() => setFlipped((f) => !f)}>
        <div className="flashcard-inner">
          <div className="flashcard-face flashcard-front">{card.definition}</div>
          <div className="flashcard-face flashcard-back">{card.term}</div>
        </div>
      </div>
      <p className="muted small">Click the card to flip</p>

      <div className="flashcard-nav">
        <button onClick={prev}>&larr; Prev</button>
        <button onClick={next}>Next &rarr;</button>
      </div>
    </div>
  );
}
