import { useState } from "react";

export default function TestViewer({ questions }) {
  const [answers, setAnswers] = useState({});

  if (!questions || questions.length === 0) {
    return <p className="muted">No test questions generated.</p>;
  }

  function selectAnswer(questionIndex, option) {
    setAnswers((prev) => ({ ...prev, [questionIndex]: option }));
  }

  const answeredCount = Object.keys(answers).length;
  const correctCount = questions.reduce(
    (acc, q, i) => acc + (answers[i] === q.correctAnswer ? 1 : 0),
    0
  );

  return (
    <div className="test-viewer">
      {answeredCount === questions.length && (
        <div className="score-banner">
          Score: {correctCount} / {questions.length}
        </div>
      )}

      {questions.map((q, qIndex) => {
        const selected = answers[qIndex];
        const isAnswered = selected !== undefined;

        return (
          <div className="question-card" key={qIndex}>
            <p className="question-text">
              {qIndex + 1}. {q.question}
            </p>
            <div className="options">
              {q.options.map((option, oIndex) => {
                let className = "option";
                if (isAnswered) {
                  if (option === q.correctAnswer) className += " correct";
                  else if (option === selected) className += " incorrect";
                }
                return (
                  <button
                    key={oIndex}
                    type="button"
                    className={className}
                    disabled={isAnswered}
                    onClick={() => selectAnswer(qIndex, option)}
                  >
                    {option}
                  </button>
                );
              })}
            </div>
            {isAnswered && <p className="explanation">{q.explanation}</p>}
          </div>
        );
      })}
    </div>
  );
}
