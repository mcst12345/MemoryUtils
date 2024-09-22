package miku.lib.jvm.hotspot.code;

//nmethod extends CodeBlob @ 296
//  Method* _method @ 64
//  int _entry_bci @ 72
//  nmethod* _osr_link @ 88
//  nmethod* _scavenge_root_link @ 96
//  address _entry_point @ 120
//  address _verified_entry_point @ 128
//  address _osr_entry_point @ 136
//  int _exception_offset @ 144
//  int _deoptimize_offset @ 148
//  int _deoptimize_mh_offset @ 152
//  int _consts_offset @ 160
//  int _stub_offset @ 164
//  int _oops_offset @ 168
//  int _metadata_offset @ 172
//  int _scopes_data_offset @ 176
//  int _scopes_pcs_offset @ 180
//  int _dependencies_offset @ 184
//  int _handler_table_offset @ 188
//  int _nul_chk_table_offset @ 192
//  int _nmethod_end_offset @ 196
//  int _orig_pc_offset @ 200
//  int _compile_id @ 204
//  int _comp_level @ 208
//  bool _marked_for_deoptimization @ 214
//  volatile unsigned char _state @ 217
//  jbyte _scavenge_root_state @ 219
//  jint _lock_count @ 224
//  long _stack_traversal_mark @ 232
//  ExceptionCache* _exception_cache @ 248

public class NMethod extends CodeBlob {
    public NMethod(long address) {
        super(address);
    }
}
