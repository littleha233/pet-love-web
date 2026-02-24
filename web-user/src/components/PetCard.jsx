const statusMap = {
  VERIFIED: "已审核",
  AVAILABLE: "可申请",
  ADOPTED: "已领养"
};

function PetCard({ pet, index = 0 }) {
  return (
    <article
      className="pet-card card fade-up"
      style={{ "--delay": `${index * 90}ms` }}
    >
      <div className="pet-image-wrap">
        <img src={pet.imageUrl} alt={pet.name} />
        <span className={`status-tag status-${pet.status.toLowerCase()}`}>
          {statusMap[pet.status]}
        </span>
      </div>
      <div className="pet-card-body">
        <h3>{pet.name}</h3>
        <p className="pet-meta">
          {pet.city} · {pet.type} · {pet.age}
        </p>
        <p>{pet.description}</p>
        <div className="tag-row">
          {pet.tags.map((tag) => (
            <span key={tag} className="soft-tag">
              {tag}
            </span>
          ))}
        </div>
        <button type="button" className="secondary-btn card-btn">
          提交领养申请
        </button>
      </div>
    </article>
  );
}

export default PetCard;
