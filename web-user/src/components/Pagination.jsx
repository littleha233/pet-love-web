function Pagination({ page, pageSize, total, onChange }) {
  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className="pagination-wrap">
      <button
        className="secondary-btn"
        type="button"
        disabled={page <= 1}
        onClick={() => onChange(page - 1)}
      >
        上一页
      </button>
      <span className="pagination-text">
        第 {page} / {totalPages} 页，共 {total} 条
      </span>
      <button
        className="secondary-btn"
        type="button"
        disabled={page >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        下一页
      </button>
    </div>
  );
}

export default Pagination;
