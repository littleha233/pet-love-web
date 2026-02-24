import { useState } from "react";

const INITIAL = {
  ratingOverall: 5,
  ratingTimeliness: 5,
  ratingCleanliness: 5,
  ratingAttitude: 5,
  content: ""
};

function FeedingReviewModal({ open, submitting, onClose, onSubmit }) {
  const [form, setForm] = useState(INITIAL);

  if (!open) {
    return null;
  }

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: name.startsWith("rating") ? Number(value) : value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await onSubmit(form);
    setForm(INITIAL);
  }

  return (
    <div className="modal-mask">
      <div className="card modal-card">
        <h3>提交评价</h3>
        <form className="stack-form" onSubmit={handleSubmit}>
          <label>
            综合评分*
            <select name="ratingOverall" value={form.ratingOverall} onChange={onChange}>
              {[5, 4, 3, 2, 1].map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </label>
          <label>
            准时性
            <select name="ratingTimeliness" value={form.ratingTimeliness} onChange={onChange}>
              {[5, 4, 3, 2, 1].map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </label>
          <label>
            清洁度
            <select name="ratingCleanliness" value={form.ratingCleanliness} onChange={onChange}>
              {[5, 4, 3, 2, 1].map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </label>
          <label>
            服务态度
            <select name="ratingAttitude" value={form.ratingAttitude} onChange={onChange}>
              {[5, 4, 3, 2, 1].map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </label>
          <label>
            评价内容
            <textarea
              name="content"
              value={form.content}
              onChange={onChange}
              maxLength={1000}
              rows={4}
            />
          </label>
          <div className="action-row">
            <button className="primary-btn" type="submit" disabled={submitting}>
              {submitting ? "提交中..." : "确认提交"}
            </button>
            <button className="secondary-btn" type="button" onClick={onClose} disabled={submitting}>
              取消
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default FeedingReviewModal;
