package br.com.necroterio.dao;

import br.com.necroterio.dao.filter.PessoaFilter;
import br.com.necroterio.dao.util.JpaUtil;
import br.com.necroterio.dto.PessoasPorEstadoDTO;
import br.com.necroterio.model.Pessoa;
import br.com.necroterio.model.enums.Estado;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PessoaDao extends GenericDao<Pessoa, Integer> {
    public PessoaDao() {
        super(Pessoa.class);
    }

    public List<Pessoa> buscar(PessoaFilter filter) {
        EntityManager manager = JpaUtil.getEntityManager();
        try {
            StringBuilder jpqlBuilder = new StringBuilder();
            jpqlBuilder.append("select p from Pessoa p ");

            jpqlBuilder.append(" where 1=1 ");

            for (Map.Entry<String, Object> entry : filter.getFiltros().entrySet()) {
                jpqlBuilder.append(" and ").append(entry.getKey()).append(" like : ").append(entry.getKey());
            }

            if (filter.getPropriedadeOrdenacao() != null) {
                jpqlBuilder.append("order by ").append(filter.getPropriedadeOrdenacao());

                if (!filter.isAscendente()) {
                    jpqlBuilder.append(" desc");
                }
            }
            TypedQuery<Pessoa> query = manager.createQuery(jpqlBuilder.toString(), Pessoa.class);

            for (Map.Entry<String, Object> entry : filter.getFiltros().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue() + "%");
            }


            //Parte de paginação
            query.setFirstResult(filter.getPrimeiroRegistro());
            query.setMaxResults(filter.getQuantidadeRegistro());
            return query.getResultList();
        } finally {
            manager.close();
        }
    }

    public int total() {
        EntityManager manager = JpaUtil.getEntityManager();
        String jpql = "select count(p) from Pessoa p";
        TypedQuery<Long> query = manager.createQuery(jpql, Long.class);
        return query.getSingleResult().intValue();
    }

    public List<PessoasPorEstadoDTO> buscaQuantidadeDefuntoPorEstado() {

        EntityManager manager = JpaUtil.getEntityManager();

        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append(" select new br.com.necroterio.dto.PessoasPorEstadoDTO( ");
        jpqlBuilder.append("   a.estado,");
        jpqlBuilder.append("   count(a.estado)");
        jpqlBuilder.append("   ) ");
        jpqlBuilder.append("   from Pessoa p");
        jpqlBuilder.append("   right join p.endereco a");
        jpqlBuilder.append("   group by a.estado");

        TypedQuery<PessoasPorEstadoDTO> query = manager.createQuery(jpqlBuilder.toString(), PessoasPorEstadoDTO.class);
        List<PessoasPorEstadoDTO> resultado = query.getResultList();

        manager.close();
        return resultado;
    }

}
