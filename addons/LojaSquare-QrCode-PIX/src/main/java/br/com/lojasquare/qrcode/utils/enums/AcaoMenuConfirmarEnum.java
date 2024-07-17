package br.com.lojasquare.qrcode.utils.enums;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.gui.acoes.AcaoStrategy;
import br.com.lojasquare.qrcode.core.gui.acoes.impl.AddQuantidadeStrategyImpl;
import br.com.lojasquare.qrcode.core.gui.acoes.impl.GerarQrcodeStrategyImpl;
import br.com.lojasquare.qrcode.core.gui.acoes.impl.RemoverQuantidadeStrategyImpl;
import br.com.lojasquare.qrcode.core.gui.acoes.impl.VoltarMenuStrategyImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcaoMenuConfirmarEnum {

    GERAR_QRCODE(new GerarQrcodeStrategyImpl(LojaSquare.getInstance())),
    AUMENTAR_QUANTIDADE(new AddQuantidadeStrategyImpl(LojaSquare.getInstance())),
    DIMINUIR_QUANTIDADE(new RemoverQuantidadeStrategyImpl(LojaSquare.getInstance())),
    VOLTAR_MENU(new VoltarMenuStrategyImpl(LojaSquare.getInstance()));

    private AcaoStrategy acao;
}
