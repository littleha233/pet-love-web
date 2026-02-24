function TrustModule({ points, timeline }) {
  return (
    <section className="trust-grid">
      <article className="card trust-panel fade-up">
        <h3>为什么可以放心托付</h3>
        <ul className="trust-points">
          {points.map((point) => (
            <li key={point.title}>
              <p>{point.title}</p>
              <span>{point.text}</span>
            </li>
          ))}
        </ul>
      </article>
      <article className="card trust-panel fade-up" style={{ "--delay": "120ms" }}>
        <h3>服务留痕示例</h3>
        <ul className="timeline">
          {timeline.map((event) => (
            <li key={event.time}>
              <span>{event.time}</span>
              <p>{event.content}</p>
            </li>
          ))}
        </ul>
      </article>
    </section>
  );
}

export default TrustModule;
